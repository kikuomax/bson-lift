package com.github.kikuomax.bsonlift

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonElement => JavaBsonElement,
  BsonInt32 => JavaBsonInt32,
  BsonString => JavaBsonString,
  BsonValue => JavaBsonValue
}
import org.specs2.Specification

/** Specification of [[BsonDocument]]. */
class BsonDocumentSpec extends Specification { def is = s2"""

Specification of BsonDocument

  Given the following document

    doc = {
      "str": "mojiretsu",
      "num": 123,
      "obj": { "x": "y" }
    }

  ${ doc.get("str") must be(`"mojiretsu"`) }
  ${ doc.get("num") must be(`123`) }
  ${ doc.get("obj") must be(`{ "x": "y" }`) }
  ${ doc.get("xyz") must beNull }

  ${ doc.getOpt("str") must beSome((a: JavaBsonValue) => a must be(`"mojiretsu"`)) }
  ${ doc.getOpt("num") must beSome((a: JavaBsonValue) => a must be(`123`)) }
  ${ doc.getOpt("obj") must beSome((a: JavaBsonValue) => a must be(`{ "x": "y" }`)) }
  ${ doc.getOpt("xyz") must beNone }

  Given a new document without "str"
  
    doc2 = doc -- "str"

  ${ doc2 must not be(doc) }
  ${ doc2.getOpt("str") must beNone }
  ${ doc2.getOpt("num") must beSome((a: JavaBsonValue) => a must be(`123`)) }
  ${ doc2.getOpt("obj") must beSome((a: JavaBsonValue) => a must be(`{ "x": "y" }`)) }

  Given a new document without "num", "obj" and "xyz":

    doc3 = doc -- ("num", "obj", "xyz")

  ${ doc3 must not be(doc) }
  ${ doc3.getOpt("str") must beSome((a: JavaBsonValue) => a must be(`"mojiretsu"`)) }
  ${ doc3.getOpt("num") must beNone }
  ${ doc3.getOpt("obj") must beNone }

"""

  // fixtures for test cases
  val `"mojiretsu"` = new JavaBsonString("mojiretsu")
  val `123` = new JavaBsonInt32(123)
  val `{ "x": "y" }` = new JavaBsonDocument("x", new JavaBsonString("y"))
  val doc = new BsonDocument(
    new JavaBsonDocument(java.util.Arrays.asList(
      new JavaBsonElement("str", `"mojiretsu"`),
      new JavaBsonElement("num", `123`),
      new JavaBsonElement("obj", `{ "x": "y" }`))))
  val doc2: BsonDocument = doc -- "str"
  val doc3: BsonDocument = doc -- ("num", "obj", "xyz")
}
