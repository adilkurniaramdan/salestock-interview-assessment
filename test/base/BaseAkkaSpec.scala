package base

import akka.actor.ActorSystem
import akka.testkit.TestKitBase
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Provides one shared actor system for the entire test, so care must be taken to keep
 * tests isolated.
 */
abstract class BaseAkkaSpec(_system: ActorSystem = ActorSystem())
  extends BaseSpec
  with TestKitBase
  with BeforeAndAfterAll {

  override implicit lazy val system = _system

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 30.seconds)
  }
}