package example.models

import scala.concurrent.ExecutionContext

import com.nadoyo.reactivemodels._


object collections {
  case class PortfolioStore(implicit val ec: ExecutionContext) extends MongoStore {
    def collectionName: String = "portfolios"
    override def indexes = List("symbol" -> 1)
  }

  implicit object StoresPortfolios extends (PortfolioStore StoresWithMongo Portfolio) {
    def s(implicit ec: ExecutionContext) = PortfolioStore()
  }

  case class QuoteStore(implicit val ec : ExecutionContext) extends MongoStore {
    def collectionName : String = "quotes"
    override def indexes = List("stock.symbol" -> 1)
  }

  implicit object StoresQuotes extends (QuoteStore StoresWithMongo Quote){
    def s(implicit ec : ExecutionContext) = QuoteStore()
  }
}
