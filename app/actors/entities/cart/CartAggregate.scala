package actors.entities.cart

import actors.{Event, UnknownCommand}
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import models.entities.reference.Product

/**
  * Created by adildramdan on 11/18/17.
  */
class CartAggregate() extends PersistentActor with ActorLogging {

  private val state   = new CartState()

  override def persistenceId: String = self.path.name

  private def updateState(evt: Event) = {
    evt match {
      case e: ProductAddedToCart      => addProductToCart(e.product, e.qty)
      case e: ProductRemovedFromCart  => removeProductFromCart(e.product, e.qty)
      case ProductCleared             => state.clear()
    }
  }

  override def receiveCommand: Receive = {
    handleAddProductCommand     orElse
    handleRemoveProductCommand  orElse
    handleClearProductCommand   orElse
    handleGetProductCommand     orElse
    rejectUnknownCommand

  }

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
  }

  private def handleAddProductCommand: Receive = {
    case m: AddProductToCart                  =>
      persistAndUpdateState(ProductAddedToCart(m.product, m.qty))
  }

  private def handleRemoveProductCommand: Receive  = {
    case m : RemoveProductFromCart if state.contains(m.product) =>
      persistAndUpdateState(ProductRemovedFromCart(m.product, m.qty))
    case m : RemoveProductFromCart =>
  }

  private def handleClearProductCommand: Receive = {
    case ClearProduct => persistAndUpdateState(ProductCleared)
  }

  private def handleGetProductCommand: Receive = {
    case GetProduct           => sender() ! ResponseProduct(state.products)
    case m: GetProductOrder   => sender() ! ResponseProductOrder(m.source, m.data, state.products)
  }

  private def addProductToCart(product: Product, qty: Int) = {
    state.add(product, qty)
  }

  private def removeProductFromCart(product: Product, qty: Int) = {
    state.remove(product, qty)
  }
  private def persistAndUpdateState(e: Event, replyTo: ActorRef = sender()) = {
    persist(e){event =>
      updateState(event)
      replyTo ! event
    }
  }

  private def rejectUnknownCommand: Receive = {
    case x => sender() ! UnknownCommand(x)
  }
}

object CartAggregate {
  def props() =
    Props(new CartAggregate())
}