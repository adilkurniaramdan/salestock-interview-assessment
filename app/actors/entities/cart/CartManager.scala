package actors.entities.cart

import actors._
import akka.actor.{ActorContext, ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
/**
  * Created by adildramdan on 11/19/17.
  */
class CartManager extends PersistentActor with ActorLogging {
  override def persistenceId: String = self.path.name

  private val state = new AggregatesHandler()(context)

  import CartManager._
  override def receiveRecover: Receive = {
    case Created(userId) => state.create(userId)
  }

  override def receiveCommand: Receive = {
    case Create(userId)     => createCart(userId)
    case Check(id)          => checkCartExistence(id)
    case Execute(id, msg)   => forwardToCart(id, msg)
  }

  private def checkCartExistence(userId: String): Unit = {
    sender() ! state.get(userId).map(_ => Exists).getOrElse(DoesNotExists)
  }

  private def createCart(userId: String): Unit = {
    if (state.existsFor(userId)) sender() ! Exists
    else {
      persist(Created(userId)) { evt =>
          state.create(evt.userId)
          sender() ! evt
      }
    }
  }

  private def forwardToCart(userId: String, message: Message): Unit = {
    log.debug("Forward {} to {}", message, "cart-aggregator-"+userId)
    state.get(userId).map(_ forward message).getOrElse(sender() ! DoesNotExists)
  }
}

object CartManager {
  final val Name  = "cart-manager"

  def props() =
    Props(new CartManager())

  case class Create(userId: String) extends Query
  case class Check(userId: String) extends Query

  case class Execute(userId: String, m: Message)

  case object Exists extends Response
  case object DoesNotExists extends Response

  case class Created(userId: String) extends Event
}


sealed class AggregatesHandler()(context: ActorContext) {

  def existsFor(userId: String): Boolean = get(userId).isDefined

  def get(userId: String): Option[ActorRef] = context.child("cart-aggregator-"+ userId)

  def findById(userId: String): Option[ActorRef] = context.child("cart-aggregator-"+ userId)

  def create(userId: String): Unit = createChildShoppingCartActor(userId)

  def createChildShoppingCartActor(userId: String): Unit = context.actorOf(CartAggregate.props(), "cart-aggregator-"+ userId)
}