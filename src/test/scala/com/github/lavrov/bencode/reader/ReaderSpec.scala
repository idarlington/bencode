package com.github.lavrov.bencode.reader

import com.github.lavrov.bencode.Bencode

import scodec.bits.ByteVector

class ReaderSpec extends munit.FunSuite:

  test("read integer") {
    val reader = readerOf[Long]
    val result = reader(Bencode.BInteger(0L))
    val expected = Right(0L)
    assertEquals(result, expected)
  }

  test("read utf8 string") {
    val reader = readerOf[String]
    val bytes = ByteVector("abc".getBytes("UTF8"))
    val result = reader(Bencode.BString(bytes))
    val expected = Right("abc")
    assertEquals(result, expected)
  }

  test("read dictionary implicitly") {
    val reader = readerOf[Map[String, Long]]
    val input = Bencode.BDictionary(("first", Bencode.BInteger(1L)))
    val result = reader(input)
    val expected = Right(Map("first" -> 1L))
    assertEquals(result, expected)
  }

  test("read dictionary") {
    val reader =
      Reader.dictionary(
        for
          a <- "a".read[Long]
          b <- "b".read[Long]
        yield
          (a, b)
      )
    val input =
      Bencode.BDictionary(
        Map(
          ("a", Bencode.BInteger(0L)),
          ("b", Bencode.BInteger(1L)),
        )
      )
    val result = reader(input)
    val expected = Right((0L, 1L))
    assertEquals(result, expected)
  }

  test("read list") {
    val reader = readerOf[List[Long]]
    {
      val input = Bencode.BList(Bencode.BInteger(0L) :: Bencode.BInteger(1L) :: Nil)
      val result = reader(input)
      val expected = Right(List(0L, 1L))
      assertEquals(result, expected)
    }
    {
      val input = Bencode.BList(Bencode.BInteger(0L) :: Bencode.BList(Nil) :: Nil)
      val result = reader(input)
      val expected = Left("BInteger is expected, BList found")
      assertEquals(result, expected)
    }
  }

  test("lift error into result") {
    val reader0 = readerOf[Long].attempt
    val result = reader0(Bencode.BInteger(0L))
    val expected = Right(Right(0L))
    assertEquals(result, expected)
  }
