package actors.entities

import actors.entities.order.Submit
import akka.actor.ActorRef
import models.entities.reference.Product
import utils.OriginalSender

package object cart{
  sealed  trait Command
  case class AddProductToCart(product: Product, qty: Int) extends Command
  case class RemoveProductFromCart(product: Product, qty: Int) extends Command
  case object ClearProduct extends Command

  sealed trait Event
  case class ProductAddedToCart(product: Product, qty: Int) extends Event
  case class ProductRemovedFromCart(product: Product, qty: Int = 1) extends Event
  case object ProductCleared extends Event

  sealed trait Query
  case object  GetProduct extends Query
  case class  GetProductOrder(source: ActorRef, data: Submit) extends Query with OriginalSender
  case class   ResponseProduct(products: List[Item])
  case class   ResponseProductOrder(source: ActorRef, data: Submit, items: List[Item])
  case class ProductNotAvailable(product: Product, qty: Int, reason: String)
}