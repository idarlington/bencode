package com.github.lavrov.bencode.format

import com.github.lavrov.bencode.Bencode

class BencodeFormatSpec extends munit.FunSuite {

  test("decode list") {
    val input = Bencode.BList(
      Bencode.BString("a") ::
        Bencode.BString("b") :: Nil
    )
    val listStringReader: BencodeFormat[List[String]] = implicitly

    assertEquals(listStringReader.read(input), Right(List("a", "b")))
  }

}
