*bson-lift* augments BSON classes provided by [Java MongoDB Driver](https://docs.mongodb.org/ecosystem/drivers/java/) with operations useful for Scala.

Who needs this library?
-----------------------

This library would fit if you
 - manipulate relatively simple objects
 - just need a thin wrapper around underlying Java classes; e.g., `org.bson.BsonValue`, `org.bson.BsonDocument`
 - are OK with the official [MongoDB Driver for Scala](https://docs.mongodb.org/ecosystem/drivers/scala/) for communication with a MongoDB server
 - are familiar with [spray-json](https://github.com/spray/spray-json) or [play-json](https://www.playframework.com/documentation/2.2.x/ScalaJson) (I do not know much about play-json though)

If you need a more sophisticated approach, [ReactiveMongo](http://reactivemongo.org) looks nice.

Though a similar work has already done by Jeff May ([Bson ADT](https://github.com/jeffmay/bson-adt)), what really I want is a thin wrapper which retains underlying Java objects and can return them without any cost.
If you do not need access to raw Java objects, Bson ADT could be a good choice.
