package com.github.kikuomax.bsonlift

import org.bson.{
  BsonArray => JavaBsonArray,
  BsonBoolean => JavaBsonBoolean,
  BsonDocument => JavaBsonDocument,
  BsonDouble => JavaBsonDouble,
  BsonElement => JavaBsonElement,
  BsonInt32 => JavaBsonInt32,
  BsonInt64 => JavaBsonInt64,
  BsonNull => JavaBsonNull,
  BsonObjectId => JavaBsonObjectId,
  BsonString => JavaBsonString,
  BsonUndefined => JavaBsonUndefined
}
import org.bson.types.ObjectId
import org.specs2.Specification
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.immutable

/**
 * Specification of the default implicit conversions from [[BsonValue]] to
 * other types.
 */
class DefaultBsonReaderSpec extends Specification { def is = s2"""

Specification of the implicit conversion of BSON values by default BsonReader

  ${ `"mojiretsu"`.as[String] must_== "mojiretsu" }

  ${ `1`.as[String] must throwA[BsonReaderException] }

  ${ `1`.as[Int] must_== 1 }
  ${ `1`.as[Long] must_== 1L }
  ${ `1`.as[Float] must_== 1.0f }
  ${ `1`.as[Double] must_== 1.0 }

  ${ `-9223372036854775808`.as[Int] must_== 0 }
  ${ `-9223372036854775808`.as[Long] must_== -9223372036854775808L }
  ${ `-9223372036854775808`.as[Float] must beCloseTo(-9.223372e+18f +/- 1.0e+11f) }
  ${ `-9223372036854775808`.as[Double] must beCloseTo(-9.22337203685478e+18 +/- 1.0e+4) }

  ${ `0.125`.as[Int] must_== 0 }
  ${ `0.125`.as[Long] must_== 0L }
  ${ `0.125`.as[Float] must_== 0.125f }
  ${ `0.125`.as[Double] must_== 0.125 }

  ${ `3.14e+307`.as[Int] must_== Int.MaxValue }
  ${ `3.14e+307`.as[Long] must_== Long.MaxValue }
  ${ `3.14e+307`.as[Float] must bePositiveInfinity }
  ${ `3.14e+307`.as[Double] must_== 3.14e+307 }

  ${ `3.14e-292`.as[Int] must_== 0 }
  ${ `3.14e-292`.as[Long] must_== 0L }
  ${ `3.14e-292`.as[Float] must_== 0.0f }
  ${ `3.14e-292`.as[Double] must_== 3.14e-292 }

  ${ `"mojiretsu"`.as[Int] must throwA[BsonReaderException] }
  ${ `[1,2,3]`.as[Long] must throwA[BsonReaderException] }
  ${ `true`.as[Float] must throwA[BsonReaderException] }
  ${ `null`.as[Double] must throwA[BsonReaderException] }

  ${ `false`.as[Boolean] must beFalse }
  ${ `true`.as[Boolean] must beTrue }

  ${ `"mojiretsu"`.as[Boolean] must throwA[BsonReaderException] }

  ${ `ObjectId("54975b23300408095a2029d7")`.as[ObjectId] must_== new ObjectId("54975b23300408095a2029d7") }
  ${ `{"x":"abc","y":"def"}`.as[ObjectId] must throwA[BsonReaderException] }

  ${ `null`.as[Option[String]] must beNone }
  ${ `undefined`.as[Option[String]] must beNone }
  ${ `"mojiretsu"`.as[Option[String]] must beSome("mojiretsu") }
  ${ `1`.as[Option[Int]] must beSome(1) }
  ${ `0.125`.as[Option[Double]] must beSome(0.125) }
  ${ `false`.as[Option[Boolean]] must beSome(false) }

  ${ `"mojiretsu"`.as[Option[Int]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[List[Int]] must
    beAnInstanceOf[List[_]] and beEqualTo(List(1, 2, 3)) }
  ${ `[]`.as[List[Int]] must
    beAnInstanceOf[List[_]] and beEqualTo(List.empty[Int]) }
  ${ `["abc","xyz"]`.as[List[String]] must
    beAnInstanceOf[List[_]] and beEqualTo(List("abc", "xyz")) }

  ${ `"mojiretsu"`.as[List[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[Vector[Int]] must
    beAnInstanceOf[Vector[_]] and beEqualTo(Vector(1, 2, 3)) }
  ${ `[]`.as[Vector[Int]] must
    beAnInstanceOf[Vector[_]] and beEqualTo(Vector.empty[Int]) }
  ${ `["abc","xyz"]`.as[Vector[String]] must
    beAnInstanceOf[Vector[_]] and beEqualTo(Vector("abc", "xyz")) }

  ${ `"mojiretsu"`.as[Vector[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[collection.Iterable[Int]] must
    beAnInstanceOf[collection.Iterable[_]] and
    beEqualTo(collection.Iterable(1, 2, 3)) }
  ${ `[]`.as[collection.Iterable[Int]] must
    beAnInstanceOf[collection.Iterable[_]] and
    beEqualTo(collection.Iterable.empty[Int]) }
  ${ `["abc","xyz"]`.as[collection.Iterable[String]] must
    beAnInstanceOf[collection.Iterable[_]] and
    beEqualTo(collection.Iterable("abc", "xyz")) }

  ${ `"mojiretsu"`.as[collection.Iterable[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[immutable.Iterable[Int]] must
    beAnInstanceOf[immutable.Iterable[_]] and
    beEqualTo(immutable.Iterable(1, 2, 3)) }
  ${ `[]`.as[immutable.Iterable[Int]] must
    beAnInstanceOf[immutable.Iterable[_]] and
    beEqualTo(immutable.Iterable.empty[Int]) }
  ${ `["abc","xyz"]`.as[immutable.Iterable[String]] must
    beAnInstanceOf[immutable.Iterable[_]] and
    beEqualTo(immutable.Iterable("abc", "xyz")) }

  ${ `"mojiretsu"`.as[immutable.Iterable[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[collection.Seq[Int]] must
    beAnInstanceOf[collection.Seq[_]] and
    beEqualTo(collection.Seq(1, 2, 3)) }
  ${ `[]`.as[collection.Seq[Int]] must
    beAnInstanceOf[collection.Seq[_]] and
    beEqualTo(collection.Seq.empty[Int]) }
  ${ `["abc","xyz"]`.as[collection.Seq[String]] must
    beAnInstanceOf[collection.Seq[_]] and
    beEqualTo(collection.Seq("abc", "xyz")) }

  ${ `"mojiretsu"`.as[collection.Seq[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[immutable.Seq[Int]] must
    beAnInstanceOf[immutable.Seq[_]] and
    beEqualTo(immutable.Seq(1, 2, 3)) }
  ${ `[]`.as[immutable.Seq[Int]] must
    beAnInstanceOf[immutable.Seq[_]] and
    beEqualTo(immutable.Seq.empty[Int]) }
  ${ `["abc","xyz"]`.as[immutable.Seq[String]] must
    beAnInstanceOf[immutable.Seq[_]] and
    beEqualTo(immutable.Seq("abc", "xyz")) }

  ${ `"mojiretsu"`.as[immutable.Seq[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[collection.IndexedSeq[Int]] must
    beAnInstanceOf[collection.IndexedSeq[_]] and
    beEqualTo(collection.IndexedSeq(1, 2, 3)) }
  ${ `[]`.as[collection.IndexedSeq[Int]] must
    beAnInstanceOf[collection.IndexedSeq[_]] and
    beEqualTo(collection.IndexedSeq.empty[Int]) }
  ${ `["abc","xyz"]`.as[collection.IndexedSeq[String]] must
    beAnInstanceOf[collection.IndexedSeq[_]] and
    beEqualTo(collection.IndexedSeq("abc", "xyz")) }

  ${ `"mojiretsu"`.as[collection.IndexedSeq[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[immutable.IndexedSeq[Int]] must
    beAnInstanceOf[immutable.IndexedSeq[_]] and
    beEqualTo(immutable.IndexedSeq(1, 2, 3)) }
  ${ `[]`.as[immutable.IndexedSeq[Int]] must
    beAnInstanceOf[immutable.IndexedSeq[_]] and
    beEqualTo(immutable.IndexedSeq.empty[Int]) }
  ${ `["abc","xyz"]`.as[immutable.IndexedSeq[String]] must
    beAnInstanceOf[immutable.IndexedSeq[_]] and
    beEqualTo(immutable.IndexedSeq("abc", "xyz")) }

  ${ `"mojiretsu"`.as[immutable.IndexedSeq[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[collection.Set[Int]] must
    beAnInstanceOf[collection.Set[_]] and
    beEqualTo(collection.Set(1, 2, 3)) }
  ${ `[]`.as[collection.Set[Int]] must
    beAnInstanceOf[collection.Set[_]] and
    beEqualTo(collection.Set.empty[Int]) }
  ${ `["abc","xyz"]`.as[collection.Set[String]] must
    beAnInstanceOf[collection.Set[_]] and
    beEqualTo(collection.Set("abc", "xyz")) }

  ${ `"mojiretsu"`.as[collection.Set[String]] must throwA[BsonReaderException] }

  ${ `[1,2,3]`.as[immutable.Set[Int]] must
    beAnInstanceOf[immutable.Set[_]] and
    beEqualTo(immutable.Set(1, 2, 3)) }
  ${ `[]`.as[immutable.Set[Int]] must
    beAnInstanceOf[immutable.Set[_]] and
    beEqualTo(immutable.Set.empty[Int]) }
  ${ `["abc","xyz"]`.as[immutable.Set[String]] must
    beAnInstanceOf[immutable.Set[_]] and
    beEqualTo(immutable.Set("abc", "xyz")) }

  ${ `"mojiretsu"`.as[immutable.Set[String]] must throwA[BsonReaderException] }

  ${ `{"x":"abc","y":"def"}`.as[collection.Map[String, String]] must
    beAnInstanceOf[collection.Map[_,_]] and
    beEqualTo(collection.Map("x" -> "abc", "y" -> "def"))}
  ${ `{}`.as[collection.Map[String, String]] must
    beAnInstanceOf[collection.Map[String, String]] and
    beEqualTo(collection.Map.empty[String, String]) }
  ${ `{"a":123,"b":-5,"c":99}`.as[collection.Map[String, Int]] must
    beAnInstanceOf[collection.Map[_,_]] and
    beEqualTo(collection.Map("a" -> 123, "b" -> -5, "c" -> 99)) }

  ${ `[1,2,3]`.as[collection.Map[String, Int]] must throwA[BsonReaderException] }

  ${ `{"x":"abc","y":"def"}`.as[immutable.Map[String, String]] must
    beAnInstanceOf[immutable.Map[_,_]] and
    beEqualTo(immutable.Map("x" -> "abc", "y" -> "def")) }
  ${ `{}`.as[immutable.Map[String, String]] must
    beAnInstanceOf[immutable.Map[_,_]] and
    beEqualTo(immutable.Map.empty[String, String]) }
  ${ `{"a":123,"b":-5,"c":99}`.as[immutable.Map[String, Int]] must
    beAnInstanceOf[immutable.Map[_,_]] and
    beEqualTo(immutable.Map("a" -> 123, "b" -> -5, "c" -> 99)) }

  ${ `[1,2,3]`.as[immutable.Map[String, Int]] must throwA[BsonReaderException] }

"""

  // fixtures
  val `"mojiretsu"` = BsonValue(new JavaBsonString("mojiretsu"))
  val `1` = BsonValue(new JavaBsonInt32(1))
  val `-9223372036854775808` =
    BsonValue(new JavaBsonInt64(-9223372036854775808L))
  val `0.125` = BsonValue(new JavaBsonDouble(0.125))
  val `3.14e+307` = BsonValue(new JavaBsonDouble(3.14e+307))
  val `3.14e-292` = BsonValue(new JavaBsonDouble(3.14e-292))
  val `false` = BsonValue(JavaBsonBoolean.FALSE)
  val `true` = BsonValue(JavaBsonBoolean.TRUE)
  val `ObjectId("54975b23300408095a2029d7")` = BsonValue(
    new JavaBsonObjectId(new ObjectId("54975b23300408095a2029d7")))
  val `null` = new BsonValue(JavaBsonNull.VALUE)
  val `undefined` = BsonValue(new JavaBsonUndefined())
  val `[1,2,3]` = BsonValue(new JavaBsonArray(
    Seq(1, 2, 3).map(new JavaBsonInt32(_))));
  val `[]` = BsonValue(new JavaBsonArray())
  val `["abc","xyz"]` = BsonValue(new JavaBsonArray(
    Seq("abc", "xyz").map(new JavaBsonString(_))))
  val `{"x":"abc","y":"def"}` = BsonValue(new JavaBsonDocument(Seq(
    new JavaBsonElement("x", new JavaBsonString("abc")),
    new JavaBsonElement("y", new JavaBsonString("def")))))
  val `{}` = BsonValue(new JavaBsonDocument)
  val `{"a":123,"b":-5,"c":99}` = BsonValue(new JavaBsonDocument(Seq(
    new JavaBsonElement("a", new JavaBsonInt32(123)),
    new JavaBsonElement("b", new JavaBsonInt32(-5)),
    new JavaBsonElement("c", new JavaBsonInt32(99)))))

  /** Expects that a given float value is positive infinity. */
  private def bePositiveInfinity = beTrue ^^ { (x: Float) => x.isPosInfinity }
}
