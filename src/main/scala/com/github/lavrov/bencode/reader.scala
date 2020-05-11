package com.github.lavrov.bencode.reader

import com.github.lavrov.bencode.Bencode

trait Reader[-I, +A]:
  def apply(i: I): Either[String, A]

def readerOf[A](using reader: Reader[Bencode, A]) = reader

def [I, A, B](reader: Reader[I, A]).map(f: A => B): Reader[I, B] = i => reader(i).map(f)

given Reader[Bencode, Long] = {
  case Bencode.BInteger(value) => Right(value)
  case other => Left(s"BInteger expected but ${other.getClass.getSimpleName} found")
}