package cli

import kaleidoscope._

sealed trait Command
case class status() extends Command
case class Call(floor: Int, direction) extends Command
case class Land(floor: Int) extends Command
case class help() extends Command
case class exit() extends Command

or translate the parsed cli command directly to the message that will be sent to actor

def parseCommand(s: String): RequestType = s match {
  case ""
}
