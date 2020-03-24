package elevatorsystem.actor

import akka.actor.Actor
import elevatorsystem.model.{ElevatorState, GetSystemStatus, ElevatorSystemConfig}

import scala.concurrent.Future

class ElevatorManager(conf: ElevatorSystemConfig) extends Actor {

  override def receive: Receive = {
    case GetSystemStatus =>
      retrieveSystemStatus pipeto sender()
  }

  def retrieveSystemStatus: Future[Seq[ElevatorState]] = ???



}
