package elevatorsystem

import cli._

import elevatorsystem.actor.ElevatorManager

import model.ElevatorSystemConfig
import model.Messages._

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._


class ElevatorSystemController(config: ElevatorSystemConfig) {

  val system = ActorSystem("ElevatorSimulation")
  val manager = system.actorOf(Props(new ElevatorManager(config)))
  implicit val timeout = Timeout(3 seconds)

  def processCommand(command: Command): Response = command match {
    case Status() =>
      Await.result((manager ? GetSystemStatus).mapTo[SystemStatus], 1 second)
    case r: Call if floorAvailable(r.floor) =>
      Await.result((manager ? LandRequest).mapTo[CallResponse], 1 second)
    case r: Land if floorAvailable(r.floor) =>
      Await.result((manager ? GetSystemStatus).mapTo[LandResponse], 1 second)
  }

  def quit: Unit = {
    Await.result(system.terminate(), 1 second)
  }

  def floorAvailable(num: String): Boolean =
    num.toInt <= config.floorCount && num.toInt >= config.floorCount

}
