package com.github.lavrov.bencode.codec

import java.nio.charset.Charset

import com.github.lavrov.bencode.Bencode
import scodec.bits.BitVector

import scala.language.experimental

class CodecSpec extends munit.FunSuite {

  given Charset = Charset.forName("UTF-8")

  test("encode/decode positive integer") {
    val encoded = encode(Bencode.BInteger(56L))
    assertEquals(decode(encoded), Right(Bencode.BInteger(56L)))
  }

  test("encode/decode negative integer") {
    val encoded = encode(Bencode.BInteger(-567L))
    assertEquals(decode(encoded), Right(Bencode.BInteger(-567L)))
  }

  test("decode byte string") {
    val result = decode(BitVector.encodeAscii("2:aa").getOrElse(???))
    val expectation = Right(Bencode.BString.fromString("aa"))
    assertEquals(result, expectation)
  }

  test("decode list") {
    val result = decode(BitVector.encodeAscii("l1:a2:bbe").getOrElse(???))
    val expectation = Right(Bencode.BList(Bencode.BString.fromString("a") :: Bencode.BString.fromString("bb") :: Nil))
    assertEquals(result, expectation)
  }

  test("decode dictionary") {
    val result = decode(BitVector.encodeAscii("d1:ai6ee").getOrElse(???))
    val expectation = Right(Bencode.BDictionary(Map("a" -> Bencode.BInteger(6))))
    assertEquals(result, expectation)
  }

  test("encode string value") {
    assertEquals(encode(Bencode.BString.fromString("test")), BitVector.encodeString("4:test").getOrElse(???))
  }

  test("encode list value") {
    val result = encode(Bencode.BList(Bencode.BString.fromString("test") :: Bencode.BInteger(10) :: Nil))
    val expectation =
      BitVector
        .encodeString("l4:testi10ee")
        .getOrElse(???)
    assertEquals(result, expectation)
  }

  test("encode and decode long list (stack safety)") {
    val data = Bencode.BList(List.fill(500)(Bencode.BInteger(0L)))
    def encoded = encode(data)
    assertEquals(decode(encoded), Right(data))
  }

}
