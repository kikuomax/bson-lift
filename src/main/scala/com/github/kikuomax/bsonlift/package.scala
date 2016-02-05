package com.github.kikuomax

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonValue => JavaBsonValue
}
import scala.language.implicitConversions

package object bsonlift {
  /**
   * Augmented `org.bson.BsonValue`.
   *
   * Any `org.bson.BsonValue` can implicitly become [[BsonValue]].
   *
   * @constructor
   * @param underlying
   *     Underlying BSON value.
   */
  implicit class BsonValue(val underlying: JavaBsonValue)

  /**
   * Augmented `org.bson.BsonDocument`.
   *
   * Any `org.bson.BsonDocument` can implicitly become [[BsonDocument]].
   *
   * @constructor
   * @param underlying
   *     Underlying BSON document.
   */
  implicit class BsonDocument(val underlying: JavaBsonDocument)

  /** Companion object of [[BsonDocument]]. */
  object BsonDocument {
    /**
     * Converts a given value into a document.
     *
     * @param value
     *     Value to be converted into a document.
     * @return
     *     Document which wraps `value`.
     * @throws org.bson.BsonInvalidOperationException
     *     If `value` is not a valid BSON document.
     */
    implicit def valueToDocument(value: JavaBsonValue): BsonDocument =
      new BsonDocument(value.asDocument)

    /**
     * Converts a given document into a value.
     *
     * @param doc
     *     Document to be converted into a value.
     * @return
     *     Value which wraps the underlying document of `doc`.
     */
    implicit def documentToValue(doc: BsonDocument): BsonValue =
      new BsonValue(doc.underlying)
  }
}
