package com.github.lavrov.bencode.writer

import com.github.lavrov.bencode.Bencode

trait Writer[-A, +O]:
  def apply(a: A): Either[String, O]

def writerOf[A](using writer: Writer[A, Bencode]): Writer[A, Bencode] = writer

object Writer:

  extension on [O, A, B](writer: Writer[A, O]):
    def comap(f: B => A): Writer[B, O] = b => writer(f(b))

  extension on [A, B](leftWriter: Writer[A, Bencode.BDictionary]):
    def zip(rightWriter: Writer[B, Bencode.BDictionary]): Writer[(A, B), Bencode.BDictionary] =
      (a, b) =>
        for
          l <- leftWriter(a)
          r <- rightWriter(b)
        yield
          new Bencode.BDictionary(l.values ++ r.values)

  given Writer[Long, Bencode] = a => Right(Bencode.BInteger(a))

  given Writer[String, Bencode] = a => Bencode.BString.fromString(a)

  given [A](using writer: Writer[A, Bencode]) as Writer[List[A], Bencode] =
    list => {
      val buffer = scala.collection.mutable.ArrayBuffer.empty[Bencode]
      var error: String | Null = null
      def recur(list: List[A]): Unit =
        if list.nonEmpty
          writer(list.head) match
          case Right(r) =>
            buffer.append(r)
            recur(list.tail)
          case Left(e) =>
            error = e
      recur(list)
      if error == null then Right(Bencode.BList(buffer.toList)) else Left(error)
    }

end Writer

extension syntax:

  def [A](fieldName: String).write(using writer: Writer[A, Bencode]): Writer[A, Bencode.BDictionary] =
    a =>
      writer(a).map { value =>
        new Bencode.BDictionary(Map((fieldName, value)))
      }
