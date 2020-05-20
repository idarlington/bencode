package com.github.lavrov.bencode.writer

import com.github.lavrov.bencode.Bencode

class WriterSpec extends munit.FunSuite:

  test("write integer") {
    val result = writerOf[Long](0L)
    assertEquals(result, Right(Bencode.BInteger(0L)))
  }

  test("write string") {
    val result = writerOf[String]("abc")
    assertEquals(result, Right(Bencode.BString.fromStringUnsafe("abc")))
  }

  test("write list") {
    val result = writerOf[List[Long]](List(0L, 1L))
    val expected =
      Right(
        Bencode.BList(List(Bencode.BInteger(0L), Bencode.BInteger(1L)))
      )
    assertEquals(result, expected)
  }

  test("writer zip") {
    val result = ("a".write[Long] zip "b".write[Long])((0L, 1L))
    val expected =
      Right(
        Bencode.BDictionary(
          ("a", Bencode.BInteger(0L)),
          ("b", Bencode.BInteger(1L)),
        )
      ) 
    assertEquals(result, expected)
  }
