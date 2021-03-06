package com.github.lavrov.bencode
package codec

import scodec.Err
import scodec.bits.BitVector

import scodec.{Attempt, Codec, DecodeResult, Decoder, Encoder, Err, SizeBound}
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._

def decode(source: BitVector): Either[BencodeCodecError, Bencode] =
  instance.decodeOnly.decodeValue(source).toEither.left.map(BencodeCodecError)

def decodeHead(source: BitVector): Either[BencodeCodecError, (BitVector, Bencode)] =
  instance
    .decode(source)
    .toEither
    .map(v => (v.remainder, v.value))
    .left
    .map(BencodeCodecError)

def encode(value: Bencode): BitVector = instance.encode(value).require

case class BencodeCodecError(error: Err) extends Throwable(error.messageWithContext)

private[bencode] val instance: Codec[Bencode] =

  // reference to `instance` to use it recursively in its definition
  val valueCodec = lazily(instance)

  // [0-9] characters
  val asciiDigit: Codec[Char] = byte.exmap(
    b => {
      val char = b.toChar
      if (char.isDigit) Attempt.Successful(char) else Attempt.Failure(Err(s"$char not a digit"))
    },
    c => Attempt.Successful(c.toByte)
  )

  // sequence of non-negative digits delimited by `delimiter`
  val positiveNumber: Codec[Long] =
    listSuccessful(asciiDigit).exmap[Long](
      {
        case Nil => Attempt.failure(Err("No digits provided"))
        case nonEmpty => Attempt.successful(nonEmpty.mkString.toLong)
      },
      integer => Attempt.successful(integer.toString.toList)
    )

  def number: Codec[Long] = {
    val positiveInverted = positiveNumber.xmap[Long](v => -v, v => -v)
    recover(constant('-'))
      .consume {
        case true => positiveInverted
        case false => positiveNumber
      } { value =>
        value < 0
      }
  }

  // actual bencode codec

  val stringCodec: Codec[Bencode.BString] =
    (positiveNumber <~ constant(':'))
      .consume { number =>
        bytes(number.toInt).xmap[Bencode.BString](
          bv => new Bencode.BString(bv),
          bs => bs.value
        )
      }(
        _.value.size
      )

  val integerCodec: Codec[Bencode.BInteger] = (constant('i') ~> number <~ constant('e')).xmap(
    number => new Bencode.BInteger(number),
    integer => integer.value
  )

  val listCodec: Codec[Bencode.BList] =
    (constant('l') ~> listSuccessful(valueCodec) <~ constant('e')).xmap(
      elems => new Bencode.BList(elems),
      list => list.values
    )

  val keyValueCodec: Codec[String ~ Bencode] = (stringCodec ~ valueCodec).xmap(
    { case (Bencode.BString(key), value) => (key.decodeAscii.getOrElse(???), value) },
    { case (key, value) => (new Bencode.BString(ByteVector.encodeAscii(key).getOrElse(???)), value) }
  )

  val dictionaryCodec: Codec[Bencode.BDictionary] =
    (constant('d') ~> listSuccessful(keyValueCodec) <~ constant('e'))
      .xmap(
        elems => new Bencode.BDictionary(elems.toMap),
        dict => dict.values.toList
      )

  def encode(value: Bencode): Attempt[BitVector] =
    value match
      case value: Bencode.BString => stringCodec.encode(value)
      case value: Bencode.BInteger => integerCodec.encode(value)
      case value: Bencode.BList => listCodec.encode(value)
      case value: Bencode.BDictionary => dictionaryCodec.encode(value)

  def decode(bits: BitVector): Attempt[DecodeResult[Bencode]] =
    byte.decode(bits).flatMap { result =>
      result.value.toChar match
        case c if c.isDigit => stringCodec.decode(bits)
        case 'i' => integerCodec.decode(bits)
        case 'l' => listCodec.decode(bits)
        case 'd' => dictionaryCodec.decode(bits)
        case _ => Attempt.failure(Err("Invalid bencode"))
    }

  Codec(encode, decode)

end instance


/**
  * Codec that encodes/decodes a `List[A]` from a `Codec[A]`.
  *
  * When encoding, each `A` in the list is encoded and all of the resulting vectors are concatenated.
  *
  * When decoding, `codec.decode` is called repeatedly until first error or
  * there are no more remaining bits and the value result of each `decode` is returned in the list.
  *
  * @param codec codec to encode/decode a single element of the sequence
  */
private def listSuccessful[A](codec: Codec[A]): Codec[List[A]] =
  new:
    def sizeBound: SizeBound = SizeBound.unknown
    def decode(bits: BitVector): Attempt[DecodeResult[List[A]]] =
    decodeCollectSuccessful[List, A](codec, None)(bits)
    def encode(value: List[A]): Attempt[BitVector] =
    Encoder.encodeSeq(codec)(value)

private def decodeCollectSuccessful[F[_], A](dec: Decoder[A], limit: Option[Int])(buffer: BitVector)(
  using factory: collection.Factory[A, F[A]]
): Attempt[DecodeResult[F[A]]] =

  val builder = factory.newBuilder
  var remaining = buffer
  var count = 0
  val maxCount = limit getOrElse Int.MaxValue
  var error = false

  while count < maxCount && !error && remaining.nonEmpty do
    dec.decode(remaining) match
      case Attempt.Successful(DecodeResult(value, rest)) =>
        builder += value
        count += 1
        remaining = rest
      case Attempt.Failure(_) =>
        error = true

  Attempt.successful(DecodeResult(builder.result, remaining))
