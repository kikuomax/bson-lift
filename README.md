*bson-lift* augments BSON classes provided by [Java MongoDB Driver](https://docs.mongodb.org/ecosystem/drivers/java/) with operations useful for Scala.

[![Build Status](https://travis-ci.org/kikuomax/bson-lift.svg?branch=master)](https://travis-ci.org/kikuomax/bson-lift)

Who needs this library?
-----------------------

This library would fit if you
 - manipulate relatively simple objects
 - just need a thin wrapper around underlying Java classes; e.g., `org.bson.BsonValue`, `org.bson.BsonDocument`
 - are OK with the official [MongoDB Driver for Scala](https://docs.mongodb.org/ecosystem/drivers/scala/) for communication with a MongoDB server
 - are familiar with [spray-json](https://github.com/spray/spray-json) or [play-json](https://www.playframework.com/documentation/2.2.x/ScalaJson) (I do not know much about play-json though)

If you need a more sophisticated approach, [ReactiveMongo](http://reactivemongo.org) looks nice.

Though a similar work has already been done by Jeff May ([Bson ADT](https://github.com/jeffmay/bson-adt)), what really I want is a thin wrapper which retains underlying Java objects and can return them without any cost.
If you do not need access to raw Java objects, Bson ADT could be a good candidate.

Prerequisites
-------------

You need the following software installed,
 - [Git](https://git-scm.com)
 - [sbt](http://www.scala-sbt.org)

Importing *bson-lift*
---------------------

The easiest way to import *bson-lift* to your project is to locally publish it.
The following are the basic steps,

 1. Clone the repository somewhere you like and move down to it.

	```shell
	git clone https://github.com/kikuomax/bson-lift.git
	cd bson-lift
	```

 2. Build and locally publish *bson-lift* by `sbt`.

	```shell
	sbt +publish-local
	```

 3. Add the following dependency to your sbt script,

	```scala
	libraryDependencies += "com.github.kikuomax" %% "bson-lift" % "0.2.1"
	```

Using *bson-lift*
-----------------

Everything is defined in the `com.github.kikuomax.bsonlift` package.

### Accessing fields

Any `org.bson.BsonDocument` can implicitly be a `com.github.kikuomax.bsonlift.BsonDocument` (say `BsonDocument`).

`BsonDocument` has `getOpt` function.

```scala
import com.github.kikuomax.bsonlift._

// example document
val doc = org.bson.BsonDocument.parse("""{
  "name": "John Flimsy",
  "weight": 110.2
}""")

doc.getOpt("name")  // --> Some(org.bson.BsonString("John Flimsy"))
doc.getOpt("weight")  // --> Some(org.bson.BsonDouble(110.2))
doc.getOpt("age")  // --> None
```

### Converting BSON to basic types

There are predefined conversions for the following basic types,
 - `String`
 - `Int`
 - `Long`
 - `Float`
 - `Double`
 - `Boolean`
 - `Option[T]`
 - `List[T]`
 - `Vector[T]`
 - `Iterable[T]`
 - `Seq[T]`
 - `IndexedSeq[T]`
 - `Set[T]`
 - `Map[String,T]`

In addition to the basic types, `org.bson.types.ObjectId` is also supported.

```scala
import com.github.kikuomax.bsonlift._

// example document
val doc = org.bson.BsonDocument.parse("""{
  "first": "John",
  "last": "Flimsy",
  "weight": 110.2,
  "height": 74.8,
  "language": ["English", "Italian", "Japanese"],
  "age": null
}""")

doc.get("first").as[String]  // --> "John"
doc.get("last").as[String]  // --> "Flimsy"
doc.get("weight").as[Double]  // --> 110.2
doc.get("height").as[Int]  // --> 74
doc.get("language").as[List[String]]  // --> List("English", "Italian", "Japanese")
doc.get("age").as[Option[Int]]  // --> None
```

### Defining a custom conversion from BSON to your type

You can define a custom conversion for your type by defining implicit `BsonReader` in the scope.

```scala
import com.github.kikuomax.bsonlift._

// example document
val doc = org.bson.BsonDocument.parse("""{
  "first": "John",
  "last": "Flimsy",
  "weight": 110.2,
  "height": 74.8,
  "language": ["English", "Italian", "Japanese"]
}""")

// custom type
case class Person(first: String, last: String, weight: Double, height: Double, language: List[String])

// implicit conversion
implicit val bsonToPerson: BsonReader[Person] = new BsonReader[Person] {
  def read(bson: org.bson.BsonValue): Person = {
    Person(
      first = bson.get("first").as[String],
      last = bson.get("last").as[String],
      weight = bson.get("weight").as[Double],
      height = bson.get("height").as[Double],
      language = bson.get("language").as[List[String]])
  }
}

doc.as[Person]  // --> Person("John", "Flimsy", 110.2, 74.8, List("English", "Italian", "Japanese"))
```

Running tests
-------------

```shell
sbt test
```

Generating documentation
------------------------

```shell
sbt doc
```

License
-------

[MIT License](https://opensource.org/licenses/MIT)
