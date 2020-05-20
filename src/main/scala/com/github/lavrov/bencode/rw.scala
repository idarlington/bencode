package com.github.lavrov.bencode.rw

import com.github.lavrov.bencode.Bencode
import com.github.lavrov.bencode.reader.{Reader, syntax}
import com.github.lavrov.bencode.writer.{Writer, syntax}

case class ReaderWriter[X, A](reader: Reader[X, A], writer: Writer[A, X])

def readerWriterOf[A](using reader: Reader[Bencode, A], writer: Writer[A, Bencode]): ReaderWriter[Bencode, A] =
  ReaderWriter(reader, writer)

object ReaderWriter:

  given [I, A](using reader: Reader[I, A], writer: Writer[A, I]) as ReaderWriter[I, A] =
    ReaderWriter(reader, writer)

  extension on [A, B](leftRW: ReaderWriter[Bencode.BDictionary, A]):
    def zip(rightRW: ReaderWriter[Bencode.BDictionary, B]): ReaderWriter[Bencode.BDictionary, (A, B)] =
      ReaderWriter(leftRW.reader zip rightRW.reader, leftRW.writer zip rightRW.writer)

end ReaderWriter

extension syntax:
  def [A](fieldName: String).readWrite(using reader: Reader[Bencode, A], writer: Writer[A, Bencode]): ReaderWriter[Bencode.BDictionary, A] = 
    ReaderWriter(
      Reader.dictionary(fieldName.read[A]),
      fieldName.write[A]
    )
