package cli

import model.Messages._
import cats.Show
import model._


trait CommandLineImplicits {

  implicit val ResponseShow = Show.show[ManagerResponse] {
    case SystemStatus(seq) => seq.foldRight("")((status, str) => str + toString(status))
    case r: CallResponse => s"Call request to floor ${r.floor.num.toString} with direction ${toString(r.direction)}" +
      s" was assigned to elevator: ${r.id.id.toString} "
    case r: LandResponse => s"Land request to floor ${r.floor.num.toString} was received by elevator: ${r.id.id.toString}"
    case r: SystemStepResponse => "The system moved forward one step in the simulation"
    case r: NoResponse => "Invalid command provided"
  }

  def toString(status: ElevatorStatus): String = {
    val id = status.id.id.toString

    val elevatorInfo: (String, String, String, String) = status.state match {
      case s: NoRequest => ("No requests received", "Current floor: " + s.currentFloor.num.toString, "", "")
      case s: Moving => ("Moving", "Next floor: " + (s.previousFloor.num+1).toString , s.direction.toString, toString(s.destinations))
      case s: Waiting => ("Waiting", "Current floor: " + s.currentFloor.num.toString, "Previous direction" + s.previousDirection.toString, toString(s.destinations))
    }

    s"""
       |--------------------------------------------------------------------------------------
       |Elevator ID: $id
       |${elevatorInfo._1}
       |${elevatorInfo._2}
       |${elevatorInfo._3}
       |---------------------
       |Requests:
       |${elevatorInfo._4}
       |---------------------------------------------------------------------------------------
    """.stripMargin
  }

  def toString(requests: Map[Floor, ReceivedRequestDirection]): String = {
    requests.foldRight("")((i, s) => s + "Floor:  " + i._1.num.toString + " ||| " + toString(i._2) + "\n")
  }

  def toString(d: ReceivedRequestDirection): String = {
    (if (d.floorRequest) " Landing Call  " else "") +
    (if (d.up) "Up  " else "") +
    (if(d.down) "Down " else "")
  }

  def toString(d: Direction): String = d match {
    case Up => "up"
    case Down => "down"
  }

}
