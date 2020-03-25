package cli

import kaleidoscope._
import model.Messages.{ElevatorRequest}

import scala.util.Try


sealed trait Command
case class status() extends Command
case class Call(floor: String, direction: String) extends Command
case class Land(floor: String) extends Command
case class Help() extends Command
case class Exit() extends Command
case class UndefinedCommand() extends Command

object Command{

  def parse(s: String): Command = s match {
    case "status" => status()
    case r"call ${floor}@(.*) ${direction}@(.*)" => Call(floor, direction)
    case r"land ${floor}@(.*)" if validateInt(floor) => Land(floor)
    case "help" => Help()
    case "exit" => Exit()
    case _ => UndefinedCommand()
  }

  def validateInt(s: String): Boolean = Try(s.toInt).isSuccess
  def validateDirection(s: String): Boolean = s == "up" || s == "down"

  def toMessageModel(c: Command): ElevatorRequest = {

  }

}

trait commandConversion


