package example
package controllers

import scala.concurrent.Future

import org.joda.time.DateTime
import play.api.libs.iteratee.{Iteratee, Enumerator, Enumeratee}
import play.api.mvc._

import com.nadoyo.reactivemodels._

import models._
import models.collections._
import models.formats._

object ViewController extends Controller{
  import scala.concurrent.ExecutionContext.Implicits.global

	def index() = Action.async{
    /*
     * Just specifying the type I want out. 
     * The rest will be inferred implicitly.
     */
    val portfolios : Enumerator[Portfolio] = MongoStorage.enumerator()

    val quotes : Enumerator[Quote] = MongoStorage.enumerator()

    val portsf = portfolios |>>> Iteratee.head
		
    val quotesf : Future[List[Quote]] = quotes &> Enumeratee.map[Quote](List(_)) |>>> Iteratee.consume[List[Quote]]()

    portsf.flatMap{ oport =>
      oport.map{ port =>
        quotesf.map{ quotes =>
          Ok(example.views.html.index(port, quotes))
        }
      }.getOrElse{
        Future{Ok(example.views.html.index(Portfolio(List()), List()))}
      }
    }
	}

	def insert() = Action.async{

    //At this stage, I shouldn't need to know 
    //what collection it goes in.
    val f = MongoStorage store (fakedata.portfolio)

    val ff = f.map{ _ =>
      MongoStorage flatstore (fakedata.quotes)
    }

    ff.map{_=>
      Ok(example.views.html.insert("inserted"))
    }
	}
}


object fakedata{
  def aapl = Stock("AAPL", "Apple Inc", "Nasdaq")
  def portfolio = Portfolio(
    List(
      aapl,
      Stock("F", "Ford Motor Company", "NYSE"),
      Stock("AMZN", "Amazon Inc", "Nasdaq")
    )
  )
  def quotes = List(
    Quote(aapl, new DateTime(), 125.50),
    Quote(aapl, new DateTime(), 124.49)
  )
}
