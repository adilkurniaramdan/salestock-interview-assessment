package actors.entities.cart

import java.util.UUID
import javax.inject.Inject

import actors.entities.cart.CartActor.AddItemToCart
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.persistence.PersistentActor

import scala.concurrent.ExecutionContext

/**
  * Created by adildramdan on 11/17/17.
  */
class CartActor() extends PersistentActor with ActorLogging{

}

object CartActor {
  final val Name  = "cart-actor"

  sealed trait Command
  case class Create(userId: Long, cartId: UUID)
  case class AddItemToCart(productId: Long, qty: Int) extends Command

  sealed trait Event
  case class Created(userId: Long, cartId: UUID)
  case class ItemAddedToCart(productId: Long, qty: Int) extends Event

}
