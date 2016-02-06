package com.github.kikuomax.bsonlift

import annotation.implicitNotFound
import org.bson.{
  BsonType => JavaBsonType,
  BsonValue => JavaBsonValue
}
import org.bson.types.ObjectId
import scala.collection.{
  breakOut,
  immutable
}
import scala.collection.generic.CanBuildFrom
import scala.collection.JavaConversions.{
  iterableAsScalaIterable,
  mapAsScalaMap
}
import scala.reflect.ClassTag

/**
 * Converter from a BSON value to another type.
 *
 * @tparam T
 *     Type of the destination value.
 */
@implicitNotFound(msg = "Cannot find BsonReader type class for ${T}")
trait BsonReader[T] {
  /**
   * Converts a given BSON value into a value of the type `T`.
   *
   * @param bson
   *     BSON value to be converted into a value of the type `T`.
   * @return
   *     Value of the type `T` equivalent to `bson`.
   * @throws BsonReaderException
   *     If `bson` is not convertible into `T`.
   */
  def read(bson: JavaBsonValue): T
}

/**
 * Companion object of [[BsonReader]].
 *
 * Provides implicit conversions from a BSON value to well-known types.
 *
 * If you want to allow `null` or `undefined` for a type `T <: AnyRef`,
 * consider to use `BsonReader[Option[T]]`.
 */
object BsonReader {
  /**
   * Implicit conversion from a BSON value to a `String`.
   *
   * Conversion fails if a given BSON value is not a string.
   */
  implicit val valueToString: BsonReader[String] = new BsonReader[String] {
    override def read(bson: JavaBsonValue): String =
      convertTo(bson)(_.isString)(_.asString.getValue)
  }

  /**
   * Implicit conversion from a BSON value to an `Int`.
   *
   * If the number is not representable by `Int`, it is rounded or truncated.
   *
   * Conversion fails if a given BSON value is not a number.
   */
  implicit val valueToInt: BsonReader[Int] = new BsonReader[Int] {
    override def read(bson: JavaBsonValue): Int =
      convertTo(bson)(_.isNumber)(_.asNumber.intValue)
  }

  /**
   * Implicit conversion from a BSON value to a `Long`.
   *
   * If the number is not representable by `Long`, it is rounded or truncated.
   *
   * Conversion fails if a given BSON value is not a number.
   */
  implicit val valueToLong: BsonReader[Long] = new BsonReader[Long] {
    override def read(bson: JavaBsonValue): Long =
      convertTo(bson)(_.isNumber)(_.asNumber.longValue)
  }

  /**
   * Implicit conversion from a BSON value to a `Float`.
   *
   * If the number is not representable by `Float`, it is rounded or truncated.
   *
   * Conversion fails if a given BSON value is not a number.
   */
  implicit val valueToFloat: BsonReader[Float] = new BsonReader[Float] {
    override def read(bson: JavaBsonValue): Float =
      convertTo(bson)(_.isNumber)(_.asNumber.doubleValue.toFloat)
  }

  /**
   * Implicit conversion from a BSON value to a `Double`.
   *
   * If the number is not representable by `Double`, it is rounded.
   *
   * Conversion fails if a given BSON value is not a number.
   */
  implicit val valueToDouble: BsonReader[Double] = new BsonReader[Double] {
    override def read(bson: JavaBsonValue): Double =
      convertTo(bson)(_.isNumber)(_.asNumber.doubleValue)
  }

  /**
   * Implicit conversion from a BSON value to a `Boolean`
   *
   * Conversion fails if a given BSON value is not a boolean.
   */
  implicit val valueToBoolean: BsonReader[Boolean] = new BsonReader[Boolean] {
    override def read(bson: JavaBsonValue): Boolean =
      convertTo(bson)(_.isBoolean)(_.asBoolean.getValue)
  }

  /**
   * Implicit conversion from a BSON object ID to an `org.bson.types.ObjectId`.
   *
   * Conversion fails if a given BSON value is not an object ID.
   */
  implicit val valueToObjectId: BsonReader[ObjectId] =
    new BsonReader[ObjectId] {
      override def read(bson: JavaBsonValue): ObjectId =
        convertTo(bson)(_.isObjectId)(_.asObjectId.getValue)
    }

  /**
   * Implicit conversion from a BSON value to an `Option`.
   *
   * If a given BSON value is `org.bson.BsonNull` or `org.bson.BsonUndefined`,
   * it becomes `None`.
   * Otherwise it becomes `Some` result of the conversion by `reader`.
   *
   * Conversion fails
   *  - if a given value is neither `org.bson.BsonNull` nor
   *    `org.bson.BsonUndefined`
   *  - or if a given value is not convertible by `reader`
   *
   * @tparam T
   *     Type of a target value to be wrapped by `Option`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to an `Option[T]`.
   */
  implicit def valueToOption[T](implicit reader: BsonReader[T]):
    BsonReader[Option[T]] = new BsonReader[Option[T]] {
      override def read(bson: JavaBsonValue): Option[T] =
        bson.getBsonType match {
          case JavaBsonType.NULL | JavaBsonType.UNDEFINED => None
          case _ => Some(reader.read(bson))
        }
    }

  /**
   * Implicit conversion from a BSON value to a `List`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `List`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `List[T]`.
   */
  implicit def valueToList[T](implicit reader: BsonReader[T]):
    BsonReader[List[T]] = valueToCollection[T, List[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `Vector`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `Vector`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `Vector[T]`.
   */
  implicit def valueToVector[T](implicit reader: BsonReader[T]):
    BsonReader[Vector[T]] = valueToCollection[T, Vector[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `collection.Iterable`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in an `collection.Iterable`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to an `collection.Iterable[T]`.
   */
  implicit def valueToCollectionIterable[T]
    (implicit reader: BsonReader[T]): BsonReader[collection.Iterable[T]] =
      new BsonReader[collection.Iterable[T]] {
        override def read(bson: JavaBsonValue): collection.Iterable[T] =
          convertTo(bson)(_.isArray)(_.asArray.map(reader.read(_)))
      }

  /**
   * Implicit conversion from a BSON value to a `collection.immutable.Iterable`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `collection.immutable.Iterable`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.immutable.Iterable[T]`.
   */
  implicit def valueToImmutableIterable[T]
    (implicit reader: BsonReader[T]): BsonReader[immutable.Iterable[T]] =
      valueToCollection[T, immutable.Iterable[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `collection.Seq`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `collection.Seq`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.Seq[T]`.
   */
  implicit def valueToCollectionSeq[T]
    (implicit reader: BsonReader[T]): BsonReader[collection.Seq[T]] =
      valueToCollection[T, collection.Seq[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `collection.immutable.Seq`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `collection.immutable.Seq`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.immutable.Seq[T]`.
   */
  implicit def valueToImmutableSeq[T]
    (implicit reader: BsonReader[T]): BsonReader[immutable.Seq[T]] =
      valueToCollection[T, immutable.Seq[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to an `collection.IndexedSeq`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in an `collection.IndexedSeq`.
   * @param reader
   *     Conversion from a BSON value into a value of the type `T`.
   * @return
   *     Conversion from a BSON value to an `collection.IndexedSeq[T]`.
   */
  implicit def valueToCollectionIndexedSeq[T]
    (implicit reader: BsonReader[T]): BsonReader[collection.IndexedSeq[T]] =
      valueToCollection[T, collection.IndexedSeq[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to
   * a `collection.immutable.IndexedSeq`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `collection.immutable.IndexedSeq`.
   * @param reader
   *     Conversion from a BSON value into a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.immutable.IndexedSeq[T]`.
   */
  implicit def valueToImmutableIndexedSeq[T]
    (implicit reader: BsonReader[T]): BsonReader[immutable.IndexedSeq[T]] =
      valueToCollection[T, immutable.IndexedSeq[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `collection.Set`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `collection.Set`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.Set[T]`.
   */
  implicit def valueToCollectionSet[T]
    (implicit reader: BsonReader[T]): BsonReader[collection.Set[T]] =
      valueToCollection[T, collection.Set[T]](breakOut)

  /**
   * Implicit conversion from a BSON value to a `immutable.Set`.
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in a `immutable.Set`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `immutable.Set[T]`.
   */
  implicit def valueToImmutableSet[T]
    (implicit reader: BsonReader[T]): BsonReader[immutable.Set[T]] =
      valueToCollection[T, immutable.Set[T]](breakOut)

  /**
   * Conversion from a BSON value to a generic collection.
   *
   * If `T` is implicitly convertible and `S` is a standard collection type,
   * you can do like the following,
   *
   *     valueToCollection[T, S](breakOut)
   *
   * Conversion fails
   *  - if a given value is not an array
   *  - or if an element in the array is not convertible by `reader`
   *
   * @tparam T
   *     Type of each element in the destination collection `S`.
   * @tparam S
   *     Type of the destination collection.
   * @param canBuildFrom
   *     Builds a collection of the type `S` from
   *     `Iterable[org.bson.BsonValue]`.
   * @param reader
   *     Conversion from  a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a collection `S`.
   */
  def valueToCollection[T, S : ClassTag]
    (canBuildFrom: CanBuildFrom[Iterable[JavaBsonValue], T, S])
    (implicit reader: BsonReader[T]): BsonReader[S] = new BsonReader[S] {
      override def read(bson: JavaBsonValue): S =
        convertTo(bson)(_.isArray)(_.asArray.map(reader.read(_))(canBuildFrom))
    }

  /**
   * Implicit conversion from a BSON value to a `collection.Map`.
   *
   * A key is a string.
   *
   * Conversion fails
   *  - if a given value is not a document
   *  - or if a value in the document is not convertible by `reader`
   *
   * @tparam T
   *     Type of each value in a `collection.Map`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `collection.Map[String, T]`.
   */
  implicit def valueToCollectionMap[T]
    (implicit reader: BsonReader[T]): BsonReader[collection.Map[String, T]] =
      new BsonReader[collection.Map[String, T]] {
        override def read(bson: JavaBsonValue): collection.Map[String, T] =
          convertTo(bson)(_.isDocument)(_.asDocument.mapValues(reader.read(_)))
      }

  /**
   * Implicit conversion from a BSON value to a `immutable.Map`.
   *
   * A key is a string.
   *
   * Conversion fails
   *  - if a given value is not a document
   *  - or if a value in the document is not convertible by `reader`
   *
   * @tparam T
   *     Type of each value in a `immutable.Map`.
   * @param reader
   *     Conversion from a BSON value to a value of the type `T`.
   * @return
   *     Conversion from a BSON value to a `immutable.Map[String, T]`.
   */
  implicit def valueToImmutableMap[T]
    (implicit reader: BsonReader[T]): BsonReader[immutable.Map[String, T]] =
      new BsonReader[immutable.Map[String, T]] {
        override def read(bson: JavaBsonValue): immutable.Map[String, T] =
          convertTo(bson)(_.isDocument)(_.asDocument.map {
            case (k, v) => (k, reader.read(v))
          }(breakOut): immutable.Map[String, T])
      }

  /**
   * Converts a given BSON value into a value of the type `T`.
   *
   * @tparam T
   * @param bson
   *     BSON value to be converted into a value of the type `T`.
   * @param p
   *     Function which determines whether `bson` is convertible into a value
   *     of the type `T`.
   * @param f
   *     Function which converts `bson` into a value of the type `T`.
   * @return
   *     Value of the type `T` equivalent to `bson`; i.e., `f(bson)`.
   * @throws BsonReaderException
   *     If `bson` is not convertible into a value of the type `T`;
   *     i.e., `p(bson) = false`.
   */
  def convertTo[T](bson: JavaBsonValue)
    (p: JavaBsonValue => Boolean)(f: JavaBsonValue => T)
      (implicit classTag: ClassTag[T]): T =
        if (p(bson)) f(bson)
        else throw BsonReaderException(
          s"${bson.getBsonType} is not convertible into $classTag")
}

/**
 * Exception thrown when a [[BsonReader]] cannot convert a BSON value.
 *
 * @constructor
 * @param message
 *     Brief explanation about the exception.
 */
case class BsonReaderException(message: String) extends Exception(message)
