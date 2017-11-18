package actors.entities.cart
import models.entities.reference.Product
/**
  * Created by adildramdan on 11/18/17.
  */

object CartMessage {
  sealed  trait Command
  case class AddProductToCart(product: Product, qty: Int) extends Command
  case class RemoveProductFromCart(product: Product, qty: Int) extends Command
  case object ClearProduct extends Command


  sealed trait Event
  case class ProductAddedToCart(product: Product, qty: Int) extends Event
  case class ProductRemovedFromCart(product: Product, qty: Int = 1) extends Event
  case object ProductCleared extends Event
}
