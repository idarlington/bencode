package com.github.lavrov.bencode.reader

import com.github.lavrov.bencode.Bencode

class ReaderSpec extends munit.FunSuite:

  test("read integer") {
    assertEquals(
      readerOf[Long](Bencode.BInteger(0L)),
      Right(0L)
    )
  }

  test("reader map") {
    assertEquals(
      readerOf[Long].map(_ + 1L)(Bencode.BInteger(0L)),
      Right(1L)
    )
  }