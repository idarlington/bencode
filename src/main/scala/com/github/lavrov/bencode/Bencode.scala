package com.github.lavrov.bencode

import scodec.bits.ByteVector

enum Bencode:
  case BString(value: ByteVector)
  case BInteger(value: Long)
  case BList(values: List[Bencode])
  case BDictionary(values: Map[String, Bencode])

object Bencode:
  
  object BString:
    def fromString(string: String): Either[String, BString] =
      ByteVector.encodeUtf8(string).left.map(_.toString).map(new Bencode.BString(_))
    def fromStringUnsafe(string: String): BString =
      new BString(ByteVector.encodeUtf8(string).getOrElse(???))
    val Empty = new BString(ByteVector.empty)

  object BDictionary:
    def apply(values: (String, Bencode)*): BDictionary = new BDictionary(values.toMap)
    val Empty: BDictionary = new BDictionary(Map.empty)
