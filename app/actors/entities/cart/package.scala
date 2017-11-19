package actors.entities

import actors.{Command, Event, Query, Response}
import actors.entities.order.Submit
import akka.actor.ActorRef
import models.entities.reference.Product
import utils.OriginalSender

package object cart{
  case class AddProductToCart(product: Product, qty: Int) extends Command
  case class RemoveProductFromCart(product: Product, qty: Int) extends Command
  case object ClearProduct extends Command

  case class ProductAddedToCart(product: Product, qty: Int) extends Event
  case class ProductRemovedFromCart(product: Product, qty: Int = 1) extends Event
  case object ProductCleared extends Event

  case object  GetProduct extends Query
  case class  GetProductOrder(source: ActorRef, data: Submit) extends Query with OriginalSender

  case class   ResponseProduct(products: List[Item]) extends Response
  case class   ResponseProductOrder(source: ActorRef, data: Submit, items: List[Item]) extends Response
  case class ProductNotAvailable(product: Product, qty: Int, reason: String) extends Response

}