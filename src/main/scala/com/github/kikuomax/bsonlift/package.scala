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
import scala.collection.{
  GenTraversableOnce,
  mutable
}
import scala.language.implicitConversions

package object bsonlift {
  /** General wrapper of an `org.bson.BsonValue`. */
  trait BsonWrapper {
    /** Underlying BSON value. */
    val underlying: JavaBsonValue
  }

  /** Companion object of [[BsonWrapper]]. */
  object BsonWrapper {
    /**
     * Conversion from a [[BsonWrapper]] to its underlying `org.bson.BsonValue`.
     */
    implicit def wrapperToJavaValue(wrapper: BsonWrapper): JavaBsonValue =
      wrapper.underlying
  }

  /**
   * Augmented `org.bson.BsonValue`.
   *
   * Any `org.bson.BsonValue` can implicitly become [[BsonValue]].
   *
   * @constructor
   * @param underlying
   *     Underlying BSON value.
   */
  implicit class BsonValue(override val underlying: JavaBsonValue)
    extends BsonWrapper
  {
    /**
     * Converts the underlying BSON value into a given type.
     *
     * @tparam T
     *     Type of the destination value.
     * @param reader
     *     Conversion from the underlying BSON value to a value of the type
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
     * Conversion from a [[BsonValue]] to its underlying `org.bson.BsonValue`.
     */
    implicit def valueToJavaValue(bson: BsonValue): JavaBsonValue =
      bson.underlying
  }

  /**
   * Augmented `org.bson.BsonDocument`.
   *
   * Any `org.bson.BsonValue` can implicitly become [[BsonDocument]].
   *
   * There are two subclasses [[ValidBsonDocument]] and [[InvalidBsonDocument]]
   * which wrap an `org.bson.BsonDocument` and a non-document
   * `org.bson.BsonValue` respectively.
   */
  trait BsonDocument extends BsonWrapper {
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
     * except the element associated with a given key.
     *
     * Has no effect and returns `this` if this document does not wrap an
     * actual document.
     *
     * @param k
     *     Key to be excluded.
     * @return
     *     New document which has the same elements as this document except
     *     the element associated with `k`.
     */
    def -(k: String): BsonDocument

    /**
     * Creates a new document which has the same elements as this document
     * except the elements associated with given keys.
     *
     * Has no effect and returns `this` if this document does not wrap an
     * actual document.
     *
     * @param k1
     *     Key to be excluded.
     * @param k2
     *     Another key to be excluded.
     * @param ks
     *     Other keys to be excluded.
     * @return
     *     New document which has the same elements as this document except
     *     the elements associated with the given keys (`k1`, `k2` and `ks`).
     */
    def -(k1: String, k2: String, ks: String*): BsonDocument

    /**
     * Creates a new document which has the same elements as this document
     * except the elements associated with given keys.
     *
     * Has no effect and returns `this` if this document does not wrap an
     * actual document.
     *
     * @param ks
     *     Keys to be excluded.
     * @return
     *     New document which has the same elements as this document except
     *     the elements associated with the keys in `ks`.
     */
    def --(ks: GenTraversableOnce[String]): BsonDocument
  }

  /** Companion object of [[BsonDocument]]. */
  object BsonDocument {
    /**
     * Conversion from a [[BsonDocument]] to a [[BsonValue]] which wraps
     * the same underlying value as the given document.
     */
    implicit def documentToValue(doc: BsonDocument): BsonValue =
      BsonValue(doc.underlying)
  }

  /**
   * Conversion from a `org.bson.BsonValue` into a [[BsonDocument]] which wraps
   * it.
   *
   * @param value
   *     Value to be converted into a [[BsonDocument]].
   * @return
   *     [[ValidBsonDocument]] which wraps `value` if `value` is actually
   *     a BSON document.
   *     [[InvalidBsonDocument]] which wraps `value` otherwise.
    */
  implicit def javaValueToDocument(value: JavaBsonValue): BsonDocument =
    if (value.isDocument) ValidBsonDocument(value.asDocument)
    else InvalidBsonDocument(value)

  /**
   * [[BsonDocument]] which wraps an `org.bson.BsonDocument`.
   *
   * @constructor
   * @param underlying
   *     Underlying BSON document.
   */
  implicit class ValidBsonDocument(override val underlying: JavaBsonDocument)
    extends BsonDocument
  {
    override def get(k: String): JavaBsonValue = underlying.get(k)

    override def getOpt(k: String): Option[JavaBsonValue] =
      Option(underlying.get(k))

    override def -(k: String): ValidBsonDocument =
      mapToDocument(mapAsScalaMap(underlying) - k)

    override def -(k1: String, k2: String, ks: String*): ValidBsonDocument =
      mapToDocument(mapAsScalaMap(underlying) - (k1, k2, ks:_*))

    override def --(ks: GenTraversableOnce[String]): ValidBsonDocument =
      mapToDocument(mapAsScalaMap(underlying) -- ks)

    /** Converts a given map to a document. */
    private def mapToDocument(map: collection.Map[String, JavaBsonValue]) =
      ValidBsonDocument(new JavaBsonDocument(map.map {
        case (k, v) => new JavaBsonElement(k, v)
      }(collection.breakOut): List[JavaBsonElement]))
  }

  /** Companion object of [[ValidBsonDocument]]. */
  object ValidBsonDocument {
    /**
     * Conversion from a [[ValidBsonDocument]] to its underlying
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
    override def -(k: String): BsonDocument = this

    /** `this` always. */
    override def -(k1: String, k2: String, ks: String*): BsonDocument = this

    /** `this` always. */
    override def --(ks: GenTraversableOnce[String]): BsonDocument = this
  }
}
