package com.github.kikuomax.bsonlift

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonElement => JavaBsonElement,
  BsonInt32 => JavaBsonInt32,
  BsonString => JavaBsonString,
  BsonValue => JavaBsonValue
}
import org.specs2.Specification

/** Specification of implicit conversions from BSON values. */
class ImplicitBsonSpec extends Specification { def is = s2"""

Specification of Implicit conversions from BSON values.

  org.bson.BsonValue --> BsonValue
  ${ `"mojiretsu"`.as[String] must_== "mojiretsu" }
  ${ `{ "x": 123 }`.as[CustomType] must_== CustomType(x=123) }

  org.bson.BsonDocument --> BsonDocument
  ${ `{ "x": 123 }`.getOpt("x").map(_.as[Int]) must beSome(123) }
  ${ (`{ "x": 123 }` - "x").getOpt("x") must beNone }

  org.bson.BsonValue --> BsonDocument
  ${ `"mojiretsu"`.getOpt("x") must beNone }

  BsonDocument --> BsonValue
  ${ `{ "y": { "x": 123 } }`.getOpt("y").map(_.as[CustomType]) must beSome(CustomType(x=123)) }

  BsonValue --> org.bson.BsonValue
  ${ BsonValue(`"mojiretsu"`).asString.getValue must_== "mojiretsu" }

  BsonDocument --> org.bson.BsonDocument
  ${ ValidBsonDocument(`{ "x": 123 }`).containsKey("x") must beTrue }

"""

  // fixtures
  val `"mojiretsu"` = new JavaBsonString("mojiretsu")
  val `{ "x": 123 }` = new JavaBsonDocument(
    java.util.Arrays.asList(new JavaBsonElement("x", new JavaBsonInt32(123))))
  val `{ "y": { "x": 123 } }` = new JavaBsonDocument(
    java.util.Arrays.asList(new JavaBsonElement("y", `{ "x": 123 }`)))

  // custom type
  case class CustomType(x: Int)

  // implicit conversion from BSON to CustomType
  implicit val bsonToCustomType: BsonReader[CustomType] =
    new BsonReader[CustomType] {
      override def read(bson: JavaBsonValue): CustomType =
        CustomType(x = bson.get("x").as[Int])
    }
}
