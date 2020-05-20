package com.github.lavrov.bencode.rw

import com.github.lavrov.bencode.Bencode

class ReaderWriterSpec extends munit.FunSuite:
  
  test("ReaderWriter zip") {
    val rw = ("a".readWrite[Long] zip "b".readWrite[Long] zip "c".readWrite[Long])
    val expected =
      Right(
        new Bencode.BDictionary(
          Map(
            "a" -> Bencode.BInteger(0L),
            "b" -> Bencode.BInteger(1L),
            "c" -> Bencode.BInteger(2L),
          )
        )
      )
    assertEquals(rw.writer(((0L, 1L), 2L)), expected)
  }

