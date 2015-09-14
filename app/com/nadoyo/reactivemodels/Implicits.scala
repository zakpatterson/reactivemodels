package com.nadoyo
package reactivemodels


import reactivemongo.bson._
import org.joda.time.DateTime

/**
 * Common formats.
 */
object Implicits{
  implicit val HasIdWriter = writer { t : HasId => BSONDocument("_id" -> t.mongoId) }

  implicit object DatetimeReader extends BSONReader[BSONDateTime, DateTime]{
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object DatetimeWriter extends BSONWriter[DateTime, BSONDateTime]{
    def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
  }

  implicit def WithIdReader[T](implicit treader : BSONDocumentReader[T]) : BSONDocumentReader[HasMongoId[T]] = new BSONDocumentReader[HasMongoId[T]]{
    def read(bson : BSONDocument) : HasMongoId[T] = {
      HasMongoId(bson.getAs[BSONObjectID]("_id").get, treader.read(bson))
    }
  }
}
