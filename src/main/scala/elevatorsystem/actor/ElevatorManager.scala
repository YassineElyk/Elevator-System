package elevatorsystem.actor

import akka.actor.Actor
import model.Messages.GetSystemStatus
import model.{ElevatorState, ElevatorSystemConfig}

import scala.concurrent.Future

class ElevatorManager(conf: ElevatorSystemConfig) extends Actor {

  override def receive: Receive = {
    case GetSystemStatus =>
      //retrieveSystemStatus pipeto sender()
  }

  def retrieveSystemStatus: Future[Seq[ElevatorState]] = ???


}
