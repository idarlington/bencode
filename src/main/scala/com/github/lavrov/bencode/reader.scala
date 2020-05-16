package com.github.lavrov.bencode.reader

import com.github.lavrov.bencode.Bencode

trait Reader[-I, +A]:
  def apply(i: I): Either[String, A]

def readerOf[A](using reader: Reader[Bencode, A]): Reader[Bencode, A] = reader

extension on [I, A, B](reader: Reader[I, A]):
  def map(f: A => B): Reader[I, B] = i => reader(i).map(f)
  def flatMap(f: A => Reader[I, B]): Reader[I, B] = i => reader(i).flatMap(a => f(a)(i))

extension on [I, R](reader: Reader[I, R]):
  def attempt: Reader[I, Either[String, R]] = i => Right(reader(i))

def dictionary[A](reader: Reader[Bencode.BDictionary, A]): Reader[Bencode, A] = {
  case d: Bencode.BDictionary => reader(d)
  case other => Left(s"BDictionary is expected, ${other.getClass.getSimpleName} found")
}

given Reader[Bencode, Long] = {
  case Bencode.BInteger(value) => Right(value)
  case other => Left(s"BInteger is expected, ${other.getClass.getSimpleName} found")
}

given Reader[Bencode, String] = {
  case Bencode.BString(bytes) => bytes.decodeUtf8.left.map(_.getMessage)
  case other => Left(s"BString is expected, ${other.getClass.getSimpleName} found")
}

given [R](using Reader[Bencode, R]) as Reader[Bencode, List[R]] = {
  case Bencode.BList(values) =>
    val buffer = scala.collection.mutable.ArrayBuffer.empty[R]
    var error: String | Null = null
    def recur(values: List[Bencode]): Unit =
      if values.nonEmpty
        readerOf[R](values.head) match
          case Right(r) =>
            buffer.append(r)
            recur(values.tail)
          case Left(e) =>
            error = e
    recur(values)
    if error == null then Right(buffer.toList) else Left(error)
  case other => Left(s"BList is expected, ${other.getClass.getSimpleName} found")
}

extension on (fieldName: String) {
  def as[R](using reader: Reader[Bencode, R]): Reader[Bencode.BDictionary, R] =
    _.values.get(fieldName) match
      case Some(value) => reader(value)
      case None => Left(s"Field '$fieldName' is not present in dictionary")
}
