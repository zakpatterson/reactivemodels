/**
 * Defines StoresInMongo typeclass for 
 * coupling encapsulated data with storage
 */

package com.nadoyo.reactivemodels

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Try, Success, Failure }
import scala.language.higherKinds

import org.joda.time.DateTime
import play.api.libs.iteratee.Enumerator
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

import scalaz._
import Scalaz._


/**
 * Parent trait representing something that can 
 * store data somewhere.
 */
trait Store

/**
 * Most generic concept of a datastore.  
 * Data can be represented as case classes, then we need only the correct
 * typeclass instances to move data in and out.
 * 
 * @param [S] The Store type
 * @param [T] The type of data stored 
 * @param [W[_]] A "writer", which can write some type for the store S.
 * @param [R[_]] A "reader", which can read data from the store S into T and maybe other types.
 */
trait Stores[S, T, W[_], R[_], Oid] {
  def s(implicit ec : ExecutionContext) : S
  def store(t: T)(implicit writable: W[T], ec : ExecutionContext): Future[Try[WithId[Oid, T]]]
  
  def flatstore(ts : Traversable[T])(implicit writable : W[T], ec : ExecutionContext) : Future[Unit] = {
    Future.sequence(ts.map(store _)).map(_ => ())
  }

  def enumerator[SO](sort : SO)(implicit reader : R[T], writer : W[SO], ec : ExecutionContext) : Enumerator[T]

  def enumerator[F, SO](filter : F, sort : SO)(implicit reader : R[T], filterwriter : W[F], sortwriter : W[SO], ec : ExecutionContext) : Enumerator[T]

  def enumerator[Q, F, SO](query : Q, filter : F, sort : SO)(implicit reader : R[T], querywriter : W[Q], filterwriter : W[F], sortwriter : W[SO], ec : ExecutionContext) : Enumerator[T]
}


/**
 * Provides static connection information.
 * Reuses reactivemongo driver instance.
 */
object MongoStore{
  lazy val driver = new MongoDriver
  def host = play.api.Play.current.configuration.getString("mongodb.host").get
  def db = play.api.Play.current.configuration.getString("mongodb.db").get
  lazy val connection = driver.connection(List(host))
}


/**
 * A MongoStore represents a collection, or a destination for a 
 * piece of data.  User specifies collection name.
 */
trait MongoStore extends Store {
  import MongoStore._
 
  implicit val ec : ExecutionContext

  lazy val db = connection(MongoStore.db)

  def collectionName: String

  def indexes : List[(String, Int)] = List()

  lazy val madeCollection = {
    import reactivemongo.api.indexes._
    val coll = db.collection(collectionName)
    indexes.map{
      case (name, dir) if dir > 0 => Index(List((name, IndexType.Ascending)))
      case (name, _) => Index(List((name, IndexType.Descending)))
    }.foreach(coll.indexesManager ensure _)
    coll
  }

  def collection = madeCollection
}


trait WithId[IdT, T]{
  def id : IdT 
  def t : T
}

case class WithMongoId[T](id : BSONObjectID, t : T) extends WithId[BSONObjectID, T]

/**
 * Now we can specify storable types in a MongoStore
 * Instances of `S StoresWithMongo T` need only to define what Store and what 
 * case class type are storable.
 * Requires BSONDocumentWriter and BSONDocumentReader to be defined on type T.
 * @param [S] the MongoStore type that can store a T
 * @param [T] the case class or other type that is storable.
 */
trait StoresWithMongo[S <: MongoStore, T] extends Stores[S, T, BSONDocumentWriter, BSONDocumentReader, BSONObjectID] {
  def store(t: T)(implicit writer: BSONDocumentWriter[T], ec : ExecutionContext): Future[Try[WithId[BSONObjectID, T]]] = {
    val id = BSONObjectID.generate
    val toInsert = BSONDocument("_id" -> id) ++ writer.write(t)
    s.collection.insert(toInsert).map(err => if (err.ok) Success(WithMongoId(id, t)) else Failure[WithId[BSONObjectID, T]](err))
  }
  
  /**
   * @param sort the Something like case class Sort(fieldname, direction)
   * @type [SO] the type representing a BSONReadable .  
   */
  def enumerator[SO](sort : SO)(implicit reader : BSONDocumentReader[T], writer : BSONDocumentWriter[SO], ec : ExecutionContext) : Enumerator[T] = 
   enumerator(BSONDocument(), BSONDocument(), sort)

  def enumerator[Q, SO](query : Q, sort : SO)(implicit reader : BSONDocumentReader[T], querywriter : BSONDocumentWriter[Q], sortwriter : BSONDocumentWriter[SO], ec : ExecutionContext) : Enumerator[T] = 
    enumerator(query, BSONDocument(), sort)
  
  def enumerator[Q, F, SO](query : Q, filter : F, sort : SO)(implicit reader : BSONDocumentReader[T], querywriter : BSONDocumentWriter[Q], filterwriter : BSONDocumentWriter[F], sortwriter : BSONDocumentWriter[SO], ec : ExecutionContext) : Enumerator[T] = 
    s.collection.find(query, filter).sort(sortwriter write sort).cursor[T].enumerate() 

  def update[Q, M](q : Q, m : M)(implicit qwriter : BSONDocumentWriter[Q], mwriter : BSONDocumentWriter[M], ec : ExecutionContext) : Future[LastError] = 
    s.collection.update(q, BSONDocument("$set" -> m))

  def remove[Q](q : Q, firstMatchOnly : Boolean = false)(implicit qwriter : BSONDocumentWriter[Q], ec : ExecutionContext) : Future[LastError] = 
    s.collection.remove(q, firstMatchOnly = firstMatchOnly)
}

trait Updater[T]{
  type S <: MongoStore
  def store : S StoresWithMongo T
  def update[Q, M](q : Q, m : M)(implicit qwriter : BSONDocumentWriter[Q], mwriter : BSONDocumentWriter[M], ec : ExecutionContext) : Future[LastError] = store.update(q,m)
  def remove[Q](q : Q, firstMatchOnly : Boolean = false)(implicit qwriter : BSONDocumentWriter[Q], ec : ExecutionContext) : Future[LastError] = store.remove(q)
}

/**
 * Container for the supported operations.  By this point we 
 * already have everything we need to pull data out of the database
 * and insert data in.  We know what types map to what collections.
 * This means all you have to do to get elements of case class A is:
 *
 * val dataEnumerator : Enumerator[A] = MongoStorage.enumerator()
 * 
 * 
 */
object MongoStorage{
  import sorting._
	def store[S <: MongoStore, T](t : T)(implicit storable : S StoresWithMongo T, writer : BSONDocumentWriter[T], ec : ExecutionContext) : Future[Try[WithId[BSONObjectID, T]]] = storable store t
  def query[Q, S <: MongoStore, T](q : Q)(implicit storable : S StoresWithMongo T, reader : BSONDocumentReader[T], writer : BSONDocumentWriter[Q], ec : ExecutionContext) : Enumerator[T] = enumerator(q, BSONDocument())
  def enumerator[S <: MongoStore, T]()(implicit storable : S StoresWithMongo T, reader : BSONDocumentReader[T], ec : ExecutionContext) : Enumerator[T] = storable enumerator Unsorted()
  def enumerator[S <: MongoStore, T, SO](sortBy : SO)(implicit storable : S StoresWithMongo T, reader : BSONDocumentReader[T], writer : BSONDocumentWriter[SO], ec : ExecutionContext) : Enumerator[T] = storable enumerator sortBy
  def enumerator[Q, S <: MongoStore, T, SO](query : Q, sortBy : SO)(implicit querywriter : BSONDocumentWriter[Q], storable : S StoresWithMongo T, reader : BSONDocumentReader[T], writer : BSONDocumentWriter[SO], ec : ExecutionContext) : Enumerator[T] = storable.enumerator(query, sortBy)
  def flatstore[S <: MongoStore, T](ts : Traversable[T])(implicit storable : S StoresWithMongo T, writer : BSONDocumentWriter[T], ec : ExecutionContext) : Future[Unit] = storable flatstore ts
  def updater[T](implicit updater : Updater[T]) : Updater[T] = updater
}

/**
 * Common formats.
 */

case class HasId(id : String){
  def mongoId : BSONObjectID = BSONObjectID.parse(id).toOption.get
}

object Implicits{
  implicit object HasIdWriter extends BSONDocumentWriter[HasId]{
    def write(hasid : HasId) : BSONDocument = BSONDocument("_id" -> hasid.mongoId)
  }

  implicit object DatetimeReader extends BSONReader[BSONDateTime, DateTime]{
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object DatetimeWriter extends BSONWriter[DateTime, BSONDateTime]{
    def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
  }
}



