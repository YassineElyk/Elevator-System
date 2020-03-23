import java.time.Duration

import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import elevatorsystem.{ElevatorConfig, ElevatorId}
import elevatorsystem.impl.Elevator

object Main extends App with LazyLogging {

  val system = ActorSystem("example-elevator")

  ElevatorConfig(ElevatorId(1), floorCount = 15, travelInterval = Duration.ofMinutes(1), unloadingInterval = Duration.ofSeconds())
  system.actorOf(Props(new Elevator()))

}
