package actors.entities.cart

import actors.entities.UnknownCommand
import actors.entities.cart.CartMessage._
import actors.entities.reference.Stock
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import models.entities.reference.Product

/**
  * Created by adildramdan on 11/18/17.
  */
class CartAggregate(val id: String, val productStockActor: ActorRef) extends PersistentActor with ActorLogging {

  private val state   = new CartState()

  override def persistenceId: String = self.path.name

  private def updateState(evt: Event) = evt match {
    case e: ProductAddedToCart      => addProductToCart(e.product, e.qty)
    case e: ProductRemovedFromCart  => removeProductFromCart(e.product, e.qty)
    case ProductCleared             => state.clear()
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
    case m: AddProductToCart                  => requestStockProduct(m.product, m.qty)
    case q@Stock.Unsuccessful(stock, reason)  => notifyProductNotAvailable(q)
    case q@Stock.Successful(stock)            => handleAddAvailableProduct(q)
  }

  private def requestStockProduct(product: Product, qty: Int) = {
    productStockActor ! Stock(sender(), product, qty)
  }

  private def notifyProductNotAvailable(failure: Stock.Unsuccessful) = {
    failure.source ! ProductNotAvailable(failure.stock.product, failure.stock.qty, failure.reason)
  }

  private def handleAddAvailableProduct(response: Stock.Successful) = {
    persistAndUpdateState(ProductAddedToCart(response.stock.product, response.stock.qty), response.source)
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
    case GetProduct => sender() ! ResponseProduct(state.products)
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
  def props(id: String, productStockActor: ActorRef) =
    Props(new CartAggregate(id, productStockActor))
}