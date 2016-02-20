package com.github.kikuomax.bsonlift

import org.bson.{
  BsonArray => JavaBsonArray,
  BsonDocument => JavaBsonDocument,
  BsonElement => JavaBsonElement,
  BsonInt32 => JavaBsonInt32,
  BsonNull => JavaBsonNull,
  BsonString => JavaBsonString,
  BsonUndefined => JavaBsonUndefined,
  BsonValue => JavaBsonValue
}
import org.specs2.Specification
import scala.collection.JavaConversions.seqAsJavaList

/** Specification of custom [[BsonReader]]. */
class CustomBsonReaderSpec extends Specification { def is = s2"""

Specification of a custom BsonReader

  Given the following BSON document
    doc = {
      "x": 0,
      "a": "",
      "s": []
    }
  ${ doc.as[CustomType] must_== CustomType(x=0, a="", s=Seq.empty[String]) }

  Given the following BSON document
    doc2 = {
      "x": 123,
      "a": "mojiretsu",
      "s": ["x", "y", "z"]
    }
  ${ doc2.as[CustomType] must_== CustomType(x=123, a="mojiretsu", s=Seq("x", "y", "z")) }

  ${ `123`.as[CustomType] must throwA[BsonReaderException] }
  ${ `"mojiretsu"`.as[CustomType] must throwA[BsonReaderException] }
  ${ `["x","y","z"]`.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc3 = {
      "x": "123",
      "a": "mojiretsu",
      "s": ["x", "y", "z"]
    }
  ${ doc3.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc4 = {
      "x": 123,
      "a": 4649,
      "s": ["x", "y", "z"]
    }
  ${ doc4.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc5 = {
      "x": 123,
      "a": "mojiretsu",
      "s": "xyz"
    }
  ${ doc5.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc6 = {
      "a": "mojiretsu",
      "s": ["x", "y", "z"]
    }
  ${ doc6.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc7 = {
      "x": 123,
      "s": ["x", "y", "z"]
    }
  ${ doc7.as[CustomType] must throwA[BsonReaderException] }

  Given the following BSON document
    doc8 = {
      "x": 123,
      "a": "mojiretsu"
    }
  ${ doc8.as[CustomType] must throwA[BsonReaderException] }

  ${ `null`.as[Option[CustomType]] must beNone }
  ${ `undefined`.as[Option[CustomType]] must beNone }

"""

  val `123` = BsonValue(new JavaBsonInt32(123))
  val `"mojiretsu"` = BsonValue(new JavaBsonString("mojiretsu"))
  val `["x","y","z"]` =
    BsonValue(new JavaBsonArray(Seq("x", "y", "z").map(new JavaBsonString(_))))
  val doc = BsonValue(new JavaBsonDocument(Seq(
    "x" -> BsonValue(new JavaBsonInt32(0)),
    "a" -> BsonValue(new JavaBsonString("")),
    "s" -> BsonValue(new JavaBsonArray())).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc2 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> `123`,
    "a" -> `"mojiretsu"`,
    "s" -> `["x","y","z"]`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc3 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> BsonValue(new JavaBsonString("123")),
    "a" -> `"mojiretsu"`,
    "s" -> `["x","y","z"]`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc4 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> `123`,
    "a" -> BsonValue(new JavaBsonInt32(4649)),
    "s" -> `["x","y","z"]`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc5 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> `123`,
    "a" -> `"mojiretsu"`,
    "s" -> BsonValue(new JavaBsonString("xyz"))).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc6 = BsonValue(new JavaBsonDocument(Seq(
    "a" -> `"mojiretsu"`,
    "s" -> `["x","y","z"]`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc7 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> `123`,
    "s" -> `["x","y","z"]`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val doc8 = BsonValue(new JavaBsonDocument(Seq(
    "x" -> `123`,
    "a" -> `"mojiretsu"`).map {
      case (k, v) => new JavaBsonElement(k, v.underlying)
    }))
  val `null` = BsonValue(JavaBsonNull.VALUE)
  val `undefined` = BsonValue(new JavaBsonUndefined())

  /** Custom type for test. */
  case class CustomType(x: Int, a: String, s: Seq[String])

  /** Implicit conversion from a BSON value to a [[CustomType]] */
  implicit val valueToCustomType: BsonReader[CustomType] =
    new BsonReader[CustomType] {
      override def read(bson: JavaBsonValue): CustomType =
        BsonReader.convertTo(bson)(_.isDocument) { bsonDoc =>
          val doc: BsonDocument = bsonDoc
          try {
            CustomType(
              x = doc.get("x").as[Int],
              a = doc.get("a").as[String],
              s = doc.get("s").as[Seq[String]])
          } catch {
            case ex: NullPointerException =>
              throw BsonReaderException(ex.getMessage)
          }
        }
    }
}
