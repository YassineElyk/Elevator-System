package elevatorsystem

import elevatorsystem.actor.ElevatorManager
import model.{Direction, Down, ElevatorId, ElevatorSystemConfig, Floor, Up}
import model.Messages._
import cli._

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * This class is an interface between the CLI and the actor system.
  */

class ElevatorSystemController(config: ElevatorSystemConfig) {

  val system = ActorSystem("ElevatorControlSystem")
  val manager = system.actorOf(Props(new ElevatorManager(config)))
  implicit val timeout = Timeout(5 seconds)

  def processCommand(command: Command): ManagerResponse = command match {
    case Status() => getSystemStatus
    case Call(floor, direction) if floorAvailable(floor) && validateDirection(floor, config.floorCount, direction) =>
      sendCallRequest(floor, direction)
    case Land(id, floor) if elevatorIdAvailable(id) && floorAvailable(floor) => sendLandRequest(id, floor)
    case Step() if config.manualStepping == true => step
    case _ => NoResponse()
  }

  /**
    * We allow ourselves to block on the futures used to interact with the actor system as the CLI will work sequentially.
    */

  def quit: Unit =
    Await.result(system.terminate(), 5 second)

  def getSystemStatus: SystemStatus =
    Await.result((manager ? GetSystemStatus).mapTo[SystemStatus], 5 second)

  def sendCallRequest(floor: String, dir: String): CallResponse =
    Await.result((manager ? CallRequest(Floor(floor.toInt), toDirection(dir))).mapTo[CallResponse], 5 second)

  def sendLandRequest(id: String, floor: String): LandResponse =
    Await.result((manager ? LandRequest(ElevatorId(id.toInt), Floor(floor.toInt))).mapTo[LandResponse], 5 second)

  def step: SystemStepResponse =
    Await.result((manager ? SystemStep).mapTo[SystemStepResponse], 5 second)

  /**
    * Helper methods to validate/convert user input.
    */

  def validateDirection(floor: String, floorCount: Int, direction: String) =
    !(floor.toInt == floorCount-1 && direction == "up") && !(floor.toInt == 0 && direction == "down")

  def elevatorIdAvailable(elevatorId: String): Boolean =
    elevatorId.toInt < config.elevatorCount && elevatorId.toInt >= 0

  def floorAvailable(num: String): Boolean =
    num.toInt < config.floorCount && num.toInt >= 0

  def toDirection(s: String): Direction ={
    s match { case "up" => Up; case "down" => Down}
  }

}
