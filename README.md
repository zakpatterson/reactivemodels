ReactiveModels
======
One crazy useful way to use reactivemongo.

You have a simple type of data you want to store.

	case class Person(name : String, email : String)

You already make a simple reader and writer for that, you use
`BSONDocumentReader[Person]` and `BSONDocumentWriter[Person]`

Now just declare a storage destination for it.

	case class PersonStore(implicit val ec: ExecutionContext) extends 	MongoStore {
    	def collectionName: String = "people"
    	override def indexes = List("name" -> 1)
  	}

And an instance which will implicitly tie the Person data type with
the PersonStore.

	implicit object personStore extends StoresWithMongo[PersonStore, Person] {
		def s(implicit ec: ExecutionContext) = PersonStore()
	}

This enables the following easiness:

	val allPeople : Enumerator[Person] = MongoStorage.enumerator()

or:
	
	MongoStorage.store(Person("Bill", "bill@bill.com"))

As you add more data types, which may map to the same collection, these functions will still work.

Querying, filtering, and sorting is easy too, just see the other enumerator functions in MongoStorage.
