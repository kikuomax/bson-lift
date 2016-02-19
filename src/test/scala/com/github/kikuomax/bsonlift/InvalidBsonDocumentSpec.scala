package com.github.kikuomax.bsonlift

import org.bson.{
  BsonArray => JavaBsonArray,
  BsonInt32 => JavaBsonInt32,
  BsonString => JavaBsonString
}
import org.specs2.Specification
import scala.collection.JavaConversions.seqAsJavaList

/** Specification of [[InvalidBsonDocument]]. */
class InvalidBsonDocumentSpec extends Specification { def is = s2"""

Specification of BsonDocument which wraps a non-document value

  Given the following BsonDocument wrapping a BSON string

    doc = "mojiretsu"

  ${ doc.underlying must be(`"mojiretsu"`) }
  ${ doc.get("key") must beNull }
  ${ doc.getOpt("key") must beNone }
  ${ (doc -- ("key")) must be(doc) }

  Given the following BsonDocument wrapping a BSON number

    doc2 = 123

  ${ doc2.underlying must be(`123`) }
  ${ doc2.get("x") must beNull }
  ${ doc2.getOpt("x") must beNone }
  ${ (doc2 -- ("x")) must be(doc2) }

  Given the following BsonDocument wrapping a BSON array

    doc3 = ["x","y","z"]

  ${ doc3.underlying must be(`["x","y","z"]`) }
  ${ doc3.get("name") must beNull }
  ${ doc3.getOpt("name") must beNone }
  ${ (doc3 -- ("name")) must be(doc3) }

"""

  // fixtures
  val `"mojiretsu"` = new JavaBsonString("mojiretsu")
  val `123` = new JavaBsonInt32(123)
  val `["x","y","z"]` = new JavaBsonArray(
    Seq("x", "y", "z").map(new JavaBsonString(_)))
  val doc = InvalidBsonDocument(`"mojiretsu"`)
  val doc2 = InvalidBsonDocument(`123`)
  val doc3 = InvalidBsonDocument(`["x","y","z"]`)
}
