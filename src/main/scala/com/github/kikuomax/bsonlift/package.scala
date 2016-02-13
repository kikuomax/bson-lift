package com.github.kikuomax

import org.bson.{
  BsonDocument => JavaBsonDocument,
  BsonElement => JavaBsonElement,
  BsonValue => JavaBsonValue
}
import scala.collection.JavaConversions.{
  mapAsScalaMap,
  seqAsJavaList
}
import scala.collection.mutable
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
  implicit class BsonValue(val underlying: JavaBsonValue) {
    /**
     * Converts the underlying BSON value into a given type.
     *
     * @tparam T
     *     Type of the destination value.
     * @param reader
     *     Converter from the underlying BSON value into a value of the type
     *     `T`.
     * @return
     *     Value of the type `T` equivalent to the underlying BSON value.
     * @throws BsonReaderException
     *     If the underlying BSON value is not convertible into `T`.
     */
    def as[T](implicit reader: BsonReader[T]): T = reader.read(underlying)
  }

  /**
   * Augmented `org.bson.BsonDocument`.
   *
   * Any `org.bson.BsonDocument` can implicitly become [[BsonDocument]].
   *
   * @constructor
   * @param underlying
   *     Underlying BSON document.
   */
  implicit class BsonDocument(val underlying: JavaBsonDocument) {
    /**
     * Returns the value associated with a given key.
     *
     * This method may return `null` to keep the transparency of the underlying
     * `org.bson.BsonDocument`.
     * Please consider about using [[getOpt]] istead.
     *
     * @param k
     *     Key associated with the requested value.
     * @return
     *     Value associated with `k`.
     *     `null` if no value is associated with `k`.
     */
    def get(k: String): JavaBsonValue = underlying.get(k)

    /**
     * Returns the optional value associated with a given key.
     *
     * @param k
     *     Key associated with the requested value.
     * @return
     *     Optional value associated with `k`.
     */
    def getOpt(k: String): Option[JavaBsonValue] = Option(underlying.get(k))

    /**
     * Creates a new document which has the same elements as this document
     * except the elements associated with given keys.
     *
     * @param ks
     *     Keys to be removed.
     * @return
     *     New document which has the same elements as this document except
     *     the elements associated with the keys in `ks`.
     */
    def --(ks: String*): JavaBsonDocument = {
      // NOTE: does not call underlying.clone() to avoid a deep copy
      val subdoc = (underlying: mutable.Map[String, JavaBsonValue]) -- ks
      new JavaBsonDocument(subdoc.map {
        case (k, v) => new JavaBsonElement(k, v)
      }(collection.breakOut): List[JavaBsonElement])
    }
  }

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
