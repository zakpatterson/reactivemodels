/**
 * Standard definitions for sorting.
 * for example, 
 * <pre>
 * case class NameSort(direction : Direction) extends FieldSort1{ def fname : String = "name"}
 * then you can pass NameSort(Direction.ASC) into MongoStorage.enumerator to get an enumerator sorted on mongo field "name".
 */

package com.nadoyo.reactivemodels

import math.Ordering

import reactivemongo.bson._
import org.joda.time.DateTime

object sorting {
  sealed trait Direction {
    def v: Int
  }
  object Direction{  
    case object ASC extends Direction { def v: Int = 1 }
    case object DESC extends Direction { def v: Int = -1 }
  }

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  trait FieldSort1 {
    def fname: String
    def direction: Direction
  }

  trait FieldSort0

  case class DateSort(direction: Direction) extends FieldSort1 { def fname: String = "data.date" }

  def fieldSort1Writer[T <: FieldSort1] = new BSONDocumentWriter[T] {
    def write(f: T) = BSONDocument(
      f.fname -> f.direction.v)
  }

  case class Unsorted() extends FieldSort0

  def fieldSort0Writer[T <: FieldSort0] = new BSONDocumentWriter[T] {
    def write(f: T) = BSONDocument()
  }

  implicit val unsortedWiter = fieldSort0Writer[Unsorted]
  implicit val dateSortWriter = fieldSort1Writer[DateSort]

}
