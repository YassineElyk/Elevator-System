package elevatorsystem.actor

import akka.actor.Actor
import elevatorsystem.model.Messages.GetSystemStatus
import elevatorsystem.model.{ElevatorState, ElevatorSystemConfig}

import scala.concurrent.Future

class ElevatorManager(conf: ElevatorSystemConfig) extends Actor {

  override def receive: Receive = {
    case GetSystemStatus =>
      //retrieveSystemStatus pipeto sender()
  }

  def retrieveSystemStatus: Future[Seq[ElevatorState]] = ???



}
