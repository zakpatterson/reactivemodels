package com.nadoyo

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONDocument, BSONObjectID}

package object reactivemodels{
  case class HasId(id : String){
    def mongoId : BSONObjectID = BSONObjectID.parse(id).toOption.get
  }
  
  def reader[A](f : BSONDocument => A) : BSONDocumentReader[A] = new BSONDocumentReader[A]{
    def read(bson : BSONDocument) : A = f(bson)
  }

  def writer[A](f : A => BSONDocument) : BSONDocumentWriter[A] = new BSONDocumentWriter[A]{
    def write(a : A) : BSONDocument = f(a)
  }
  
  
  trait WithId[IdT, T]{ self =>
    def id : IdT
    def t : T
    def map[A](f : T => A) : WithId[IdT,A] = new WithId[IdT, A]{
      def id = self.id
      def t = f(self.t)
    }
    def flatMap[A](f : T => WithId[IdT, A]) : WithId[IdT, A] = f(self.t)
  }

  case class HasMongoId[T](id : BSONObjectID, t : T) extends WithId[BSONObjectID,T]

  implicit def hasMongoIdWriter[T](implicit writer : BSONDocumentWriter[T]) = new BSONDocumentWriter[HasMongoId[T]]{
    def write(t : HasMongoId[T]) : BSONDocument = {
      writer.write(t.t) ++ BSONDocument("_id" -> t.id)
    }
  }

  implicit def hasMongoIdReader[T](implicit reader : BSONDocumentReader[T]) = new BSONDocumentReader[HasMongoId[T]]{
    def read(bson : BSONDocument) : HasMongoId[T] = {
      HasMongoId(bson.getAs[BSONObjectID]("_id").get, reader.read(bson))
    }
  }
}