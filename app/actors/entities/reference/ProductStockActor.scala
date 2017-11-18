package actors.entities.reference

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.pipe
import repositories.reference.ProductRepository

import scala.concurrent.ExecutionContext

/**
  * Created by adildramdan on 11/17/17.
  */
class ProductStockActor @Inject()(productRepository: ProductRepository)(implicit ec: ExecutionContext) extends Actor with ActorLogging{
  def receive = LoggingReceive {
    case m: Stock =>
      checkAvailability(m)
        .pipeTo(sender())
  }

  private def checkAvailability(m: Stock) = {
    productRepository
      .findById(m.product.id.get)
      .map{
        case Some(product) if product.qty >= m.qty  => Stock.Successful(m)
        case _                                      => Stock.Unsuccessful(m, "Sold out")
      }
  }
}


object ProductStockActor {
  final val Name  = "product-stock-actor"

  def props(productRepository: ProductRepository)(implicit ec: ExecutionContext) =
    Props(new ProductStockActor(productRepository))
}
