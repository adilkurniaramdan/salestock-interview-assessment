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
class ProductCheckerActor @Inject()(productRepository: ProductRepository)(implicit ec: ExecutionContext) extends Actor with ActorLogging{

  import ProductCheckerActor._
  def receive = LoggingReceive {
    case m: Check =>
      checkAvailability(m.id, m.qty)
        .pipeTo(sender())
  }

  private def checkAvailability(id: Long, qty: Int) = {
    productRepository
      .findById(id)
      .map{
        case Some(product) if product.qty >= qty => Available
        case _                                   => NotAvailable
      }
  }
}


object ProductCheckerActor {
  final val Name  = "product-checker-actor"

  def props(productRepository: ProductRepository)(implicit ec: ExecutionContext) =
    Props(new ProductCheckerActor(productRepository))

  sealed trait Command
  case class Check(id: Long, qty: Int = 1) extends Command

  sealed trait Event
  case object Available extends Event
  case object NotAvailable extends Event
}
