package actors.entities.reference

import javax.inject.Inject

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive

import scala.concurrent.ExecutionContext

/**
  * Created by adildramdan on 11/17/17.
  */
class OrderProcessorActor @Inject()()(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  override def receive: Receive = LoggingReceive {

  }
}

object OrderProcessorActor {
  sealed trait Command
  case class PlaceOrder() extends Command

  sealed trait Event
  case class OrderPlaced() extends Event


}
