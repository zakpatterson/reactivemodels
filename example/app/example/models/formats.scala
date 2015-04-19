package example.models

import com.nadoyo.reactivemodels._
import com.nadoyo.reactivemodels.Implicits._

import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocument, BSONDocumentWriter, BSONDocumentReader}
import scalaz._
import Scalaz._


object formats {
  implicit val stockWriter = new BSONDocumentWriter[Stock] {
    def write(s: Stock) = BSONDocument(
      "symbol" -> s.symbol,
      "name" -> s.name,
      "exchange" -> s.exchange)
  }

  implicit val stockReader = new BSONDocumentReader[Stock] {
    def read(bson: BSONDocument): Stock = (
      bson.getAs[String]("symbol") |@|
      bson.getAs[String]("name") |@|
      bson.getAs[String]("exchange"))((a, b, c) => Stock(a, b, c)).get
  }

  implicit val portfolioWriter = new BSONDocumentWriter[Portfolio] {
    def write(p: Portfolio) = BSONDocument(
      "stocks" -> p.stocks)
  }

  implicit val portfolioReader = new BSONDocumentReader[Portfolio] {
    def read(bson: BSONDocument) = bson.getAs[List[Stock]]("stocks").map(Portfolio apply _).get
  }

  implicit val quoteWriter = new BSONDocumentWriter[Quote] {
    def write(s: Quote) = BSONDocument(
      "stock" -> s.stock,
      "date" -> s.date,
      "price" -> s.price
    )
  }
  
  implicit val quoteReader = new BSONDocumentReader[Quote] {
    def read(bson: BSONDocument): Quote = (bson.getAs[Stock]("stock") |@|
      bson.getAs[DateTime]("date") |@|
      bson.getAs[Double]("price"))((a, b, c) => Quote(a, b, c)).get
  }

}
