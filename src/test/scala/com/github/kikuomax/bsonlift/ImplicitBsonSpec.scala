package com.github.kikuomax.bsonlift

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonInvalidOperationException,
  BsonString => JavaBsonString,
  BsonValue => JavaBsonValue
}
import org.specs2.Specification

/** Specification of implicit conversions from BSON values. */
class ImplicitBsonSpec extends Specification { def is = s2"""

Specification of Implicit conversions from BSON values.

  org.bson.BsonValue can implicitly become a BsonValue  ${valueToValueTest}
  org.bson.BsonDocument can implicitly become a BsonDocument  ${documentToDocumentTest}
  org.bson.BsonValue can implicitly become a BsonDocument  ${valueToDocumentTest}
  BsonDocument can implicitly become a BsonValue  ${documentToValueTest}
  org.bson.BsonInvalidOperationException should be thrown
  if a non-document org.bson.BsonValue tries to become a BsonDocument  ${improperValueToDocument}

"""

  def valueToValueTest = {
    val java: JavaBsonValue = new JavaBsonString("test")
    val scala: BsonValue = java
    success
  }

  def documentToDocumentTest = {
    val java: JavaBsonDocument = new JavaBsonDocument()
    val scala: BsonDocument = java
    success
  }

  def valueToDocumentTest = {
    val value: JavaBsonValue = new JavaBsonDocument()
    val doc: BsonDocument = value
    success
  }

  def documentToValueTest = {
    val doc: BsonDocument = new BsonDocument(new JavaBsonDocument())
    val value: BsonValue = doc
    success
  }

  def improperValueToDocument = {
    val value: JavaBsonValue = new JavaBsonString("test")
    (value: BsonDocument) must throwA[BsonInvalidOperationException]
  }
}
