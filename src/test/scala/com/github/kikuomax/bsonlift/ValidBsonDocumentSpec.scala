package com.github.kikuomax.bsonlift

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonElement => JavaBsonElement,
  BsonInt32 => JavaBsonInt32,
  BsonString => JavaBsonString,
  BsonValue => JavaBsonValue
}
import org.specs2.Specification

/** Specification of [[ValidBsonDocument]]. */
class ValidBsonDocumentSpec extends Specification { def is = s2"""

Specification of BsonDocument which wraps a valid document

  Given the following BsonDocument

    doc = {
      "str": "mojiretsu",
      "num": 123,
      "obj": { "x": "y" }
    }

  ${ doc.get("str") must be(`"mojiretsu"`) }
  ${ doc.get("num") must be(`123`) }
  ${ doc.get("obj") must be(`{ "x": "y" }`) }
  ${ doc.get("xyz") must beNull }

  ${ doc.getOpt("str") must beSomeBsonValue(`"mojiretsu"`) }
  ${ doc.getOpt("num") must beSomeBsonValue(`123`) }
  ${ doc.getOpt("obj") must beSomeBsonValue(`{ "x": "y" }`) }
  ${ doc.getOpt("xyz") must beNone }

  Given a new BsonDocument without "str"
  
    doc2 = doc -- "str"

  ${ doc2 must not be(doc) }

  ${ doc2.get("str") must beNull }
  ${ doc2.get("num") must be(`123`) }
  ${ doc2.get("obj") must be(`{ "x": "y" }`) }

  ${ doc2.getOpt("str") must beNone }
  ${ doc2.getOpt("num") must beSomeBsonValue(`123`) }
  ${ doc2.getOpt("obj") must beSomeBsonValue(`{ "x": "y" }`) }

  Given a new BsonDocument without "num", "obj" and "xyz":

    doc3 = doc -- ("num", "obj", "xyz")

  ${ doc3 must not be(doc) }

  ${ doc3.get("str") must be(`"mojiretsu"`) }
  ${ doc3.get("num") must beNull }
  ${ doc3.get("obj") must beNull }

  ${ doc3.getOpt("str") must beSomeBsonValue(`"mojiretsu"`) }
  ${ doc3.getOpt("num") must beNone }
  ${ doc3.getOpt("obj") must beNone }

  Given the following BsonDocument:

    doc4 = { "x": "y" }

  ${ doc4.underlying must be(`{ "x": "y" }`) }

"""

  // fixtures for test cases
  val `"mojiretsu"` = new JavaBsonString("mojiretsu")
  val `123` = new JavaBsonInt32(123)
  val `{ "x": "y" }` = new JavaBsonDocument("x", new JavaBsonString("y"))
  val doc = ValidBsonDocument(
    new JavaBsonDocument(java.util.Arrays.asList(
      new JavaBsonElement("str", `"mojiretsu"`),
      new JavaBsonElement("num", `123`),
      new JavaBsonElement("obj", `{ "x": "y" }`))))
  val doc2 = ValidBsonDocument(doc -- "str")
  val doc3 = ValidBsonDocument(doc -- ("num", "obj", "xyz"))
  val doc4 = ValidBsonDocument(`{ "x": "y" }`)

  /** Expectation for some raw BSON value. */
  def beSomeBsonValue(b: JavaBsonValue) =
    beSome((a: JavaBsonValue) => a must be(b))
}
