package elevatorsystem

import akka.actor.{ActorSystem, Props}
import elevatorsystem.actor.ElevatorManager
import model.ElevatorSystemConfig

class ElevatorSystemController(config: ElevatorSystemConfig) {

  val system = ActorSystem("Elevator Simulation")
  val manager = system.actorOf(Props(new ElevatorManager(config)))


}
