package utils

import akka.actor.ActorRef

/**
  * Created by adildramdan on 11/18/17.
  */
trait OriginalSender {

  def source: ActorRef

}
