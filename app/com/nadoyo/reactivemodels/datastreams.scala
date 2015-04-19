package com.nadoyo.reactivemodels

import scala.concurrent.ExecutionContext

import play.api.libs.iteratee.Enumerator
import reactivemongo.bson._

import sorting._
import Implicits._

/**
 * Sample enumerators
 */
trait DataStreams{
  def sortedByDate[T, S <: MongoStore](implicit ev : S StoresWithMongo T, rev : BSONDocumentReader[T], ec : ExecutionContext) : Enumerator[T] = MongoStorage.enumerator(DateSort(Direction.ASC))
  def unsorted[T, S <: MongoStore](implicit ev : S StoresWithMongo T, rev : BSONDocumentReader[T], ec : ExecutionContext) : Enumerator[T] = MongoStorage.enumerator(Unsorted())
}
