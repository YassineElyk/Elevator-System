package cli

import kaleidoscope._

import scala.util.Try


sealed trait Command
case class Status() extends Command
case class Call(floor: String, direction: String) extends Command
case class Land(floor: String) extends Command
case class Help() extends Command
case class Exit() extends Command
case class UndefinedCommand() extends Command

object Command{

  def parse(s: String): Command = s match {
    case "status" => Status()
    case r"call ${floor}@(.*) ${direction}@(.*)" if validateInt(floor) && validateDirection(direction) => Call(floor, direction)
    case r"land ${floor}@(.*)" if validateInt(floor) => Land(floor)
    case "help" => Help()
    case "exit" => Exit()
    case _ => UndefinedCommand()
  }

  def validateInt(s: String): Boolean = Try(s.toInt).isSuccess
  def validateDirection(s: String): Boolean = s == "up" || s == "down"

}


