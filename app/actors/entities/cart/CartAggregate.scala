package actors.entities.cart

import actors.entities.cart.CartMessage._
import akka.actor.{ActorLogging, ActorRef}
import akka.persistence.PersistentActor
import models.entities.reference.Product

/**
  * Created by adildramdan on 11/18/17.
  */
class CartAggregate(val id: String, val productActor: ActorRef) extends PersistentActor with ActorLogging {

  private val state   = new CartState()

  override def persistenceId: String = self.path.name

  private def updateState(evt: Event) = evt match {
    case e: ProductAddedToCart      => addProductToCart(e.product, e.qty)
    case e: ProductRemovedFromCart  => removeProductFromCart(e.product, e.qty)
    case ProductCleared             => state.clear()
  }

  override def receiveCommand: Receive = ???

  override def receiveRecover: Receive = ???

  private def handleAddProductCommand: Receive = {
    case m: AddProductToCart  =>
  }

  private def requestForAvaialabilityProduct(product: Product, qty: Int) = {
    productActor !
  }



  private def addProductToCart(product: Product, qty: Int) = {
    state.add(product, qty)
  }

  private def removeProductFromCart(product: Product, qty: Int) = {
    state.remove(product, qty)
  }


}