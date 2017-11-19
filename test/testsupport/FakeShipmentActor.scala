package testsupport

import actors.entities.order.ShipmentActor.{RequestShipmentID, ResponseShipmentID}
import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive

/**
  * Created by adildramdan on 11/18/17.
  */
class FakeShipmentActor extends Actor with ActorLogging {

  def receive = LoggingReceive {
    case m: RequestShipmentID  =>
      sender() ! ResponseShipmentID(m.source, m.data, "THIS_IS_RANDOM_RESULT")
  }
}

object FakeShipmentActor {
  final val Name  = "fake-shipment-actor"

  def props() = Props(new FakeShipmentActor())
}
