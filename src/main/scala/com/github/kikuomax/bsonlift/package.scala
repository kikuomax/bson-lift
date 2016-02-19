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

  /** Companion object of [[BsonValue]]. */
  object BsonValue {
    /**
     * Converts a given [[BsonValue]] into its underlying `org.bson.BsonValue`.
     */
    implicit def valueToJavaValue(bson: BsonValue): JavaBsonValue =
      bson.underlying
  }

  /**
   * Augmented `org.bson.BsonDocument`.
   *
   * Any `org.bson.BsonValue` can implicitly become [[BsonDocument]].
   *
   * There is a subclass [[ValidBsonDocument]] which wraps an
   * `org.bson.BsonDocument`.
   * There also is a subclass [[InvalidBsonDocument]] which wraps a non-document
   * value.
   */
  sealed trait BsonDocument {
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
     *     `null` if no value is associated with `k`,
     *     or if this document does not wrap a valid document.
     */
    def get(k: String): JavaBsonValue

    /**
     * Returns the optional value associated with a given key.
     *
     * @param k
     *     Key associated with the requested value.
     * @return
     *     Optional value associated with `k`.
     *     `None` if no value is associated with `k`,
     *     or if this document does not wrap a valid document.
     */
    def getOpt(k: String): Option[JavaBsonValue]

    /**
     * Creates a new document which has the same elements as this document
     * except the elements associated with given keys.
     *
     * Has no effect if this document does not wrap a valid document.
     *
     * @param ks
     *     Keys to be removed.
     * @return
     *     New document which has the same elements as this document except
     *     the elements associated with the keys in `ks`.
     *     `this` instance if this document does not wrap a valid document.
     */
    def --(ks: String*): BsonDocument

    /** Underlying *raw* BSON value of this document. */
    def underlying: JavaBsonValue
  }

  /** Converts a given `org.bson.BsonValue` into a [[BsonDocument]]. */
  implicit def javaValueToDocument(value: JavaBsonValue): BsonDocument =
    BsonDocument.javaValueToDocument(value)

  /**
   * Converts a given `org.bson.BsonDocument` into a [[ValidBsonDocument]].
   */
  implicit def javaDocumentToDocument(doc: JavaBsonDocument):
    ValidBsonDocument = ValidBsonDocument(doc)

  /** Companion object of [[BsonDocument]]. */
  object BsonDocument {
    /**
     * Converts a given [[BsonDocument]] document into its underlying
     * `org.bson.BsonValue`.
     */
    implicit def documentToJavaValue(doc: BsonDocument): JavaBsonValue =
      doc.underlying

    /**
     * Converts a given value into a [[BsonDocument]].
     *
     * @param value
     *     Value to be converted into a [[BsonDocument]].
     * @return
     *     [[ValidBsonDocument]] which wraps `value` if `value` is a BSON
     *     document.
     *     [[InvalidBsonDocument]] otherwise.
     */
    implicit def javaValueToDocument(value: JavaBsonValue): BsonDocument =
      if (value.isDocument) ValidBsonDocument(value.asDocument)
      else InvalidBsonDocument(value)

    /**
     * Converts a given [[BsonDocument]] into a [[BsonValue]] which wraps
     * the same underlying value as the document.
     */
    implicit def documentToValue(doc: BsonDocument): BsonValue =
      new BsonValue(doc.underlying)
  }

  /**
   * [[BsonDocument]] which wraps an `org.bson.BsonDocument`.
   *
   * @constructor
   * @param underlying
   *     Underlying BSON document.
   */
  case class ValidBsonDocument(override val underlying: JavaBsonDocument)
    extends BsonDocument
  {
    override def get(k: String): JavaBsonValue = underlying.get(k)

    override def getOpt(k: String): Option[JavaBsonValue] =
      Option(underlying.get(k))

    override def --(ks: String*): ValidBsonDocument = {
      // NOTE: does not call underlying.clone() to avoid a deep copy
      val subdoc = (underlying: mutable.Map[String, JavaBsonValue]) -- ks
      ValidBsonDocument(new JavaBsonDocument(subdoc.map {
        case (k, v) => new JavaBsonElement(k, v)
      }(collection.breakOut): List[JavaBsonElement]))
    }
  }

  /** Companion object of [[ValidBsonDocument]]. */
  object ValidBsonDocument {
    /**
     * Converts a [[ValidBsonDocument]] to its underlying
     * `org.bson.BsonDocument`.
     */
    implicit def documentToJavaDocument(doc: ValidBsonDocument):
      JavaBsonDocument = doc.underlying
  }

  /**
   * [[BsonDocument]] which wraps a non-document `org.bson.BsonValue`.
   *
   * @constructor
   * @param underlying
   *     Underlying BSON value.
   */
  case class InvalidBsonDocument(override val underlying: JavaBsonValue)
    extends BsonDocument
  {
    /** `null` always. */
    override def get(k: String): JavaBsonValue = null

    /** `None` always. */
    override def getOpt(k: String): Option[JavaBsonValue] = None

    /** `this` always. */
    override def --(ks: String*): BsonDocument = this
  }
}
