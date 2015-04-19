package example.models

import org.joda.time.DateTime

case class Stock(symbol: String, name: String, exchange: String)
case class Portfolio(stocks: List[Stock])
case class Quote(stock: Stock, date: DateTime, price: Double)
