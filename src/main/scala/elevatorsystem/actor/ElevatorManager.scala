package elevatorsystem.actor

import akka.actor.Actor
import model.Messages.{GetSystemStatus, SystemStatus}
import model.{ElevatorState, ElevatorSystemConfig}

import scala.concurrent.Future

class ElevatorManager(conf: ElevatorSystemConfig) extends Actor {

  override def preStart(): Unit = {

  }

  override def receive: Receive = {
    case GetSystemStatus => sender ! SystemStatus("TEST COMPLETED SUCCESSFULLY")
      //retrieveSystemStatus pipeto sender()
  }

  def retrieveSystemStatus: Future[Seq[ElevatorState]] = ???

}
