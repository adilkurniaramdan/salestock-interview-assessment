package testsupport

import akka.actor.ActorSystem

object ActorSystems {
  def InMemoryPersistence = ActorSystem("InMemory", ActorSpecConfiguration())
}
