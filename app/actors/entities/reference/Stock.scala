package actors.entities.reference

import akka.actor.ActorRef
import models.entities.reference.Product

/**
  * Created by adildramdan on 11/18/17.
  */


trait OriginalSender {

  def source: ActorRef

}

case class Stock(source: ActorRef, product: Product, qty: Int) extends OriginalSender

object Stock {

  sealed abstract class Response(stock: Stock) extends OriginalSender {

    lazy val source = stock.source
  }

  case class Successful(stock: Stock) extends Response(stock)

  case class Unsuccessful(stock: Stock, reason: String) extends Response(stock)

}
