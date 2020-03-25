package cli

import elevatorsystem.ElevatorSystemController

import model.Messages._

import cats.syntax.show._

class CommandLineInterface(controller: ElevatorSystemController) extends CommandLineImplicits{

  def start: Unit = {
    println("Elevator simulation is running")
    println("-------------------------------")
    mainLoop
  }

  def mainLoop: Unit = {
    Command.parse(getConsoleInput) match {
      case Exit() =>
        println("Shutting down Elevator Simulation")
        controller.quit
      case Help() =>
        printHelp
        mainLoop
      case UndefinedCommand() =>
        println("Invalid command was provided")
        mainLoop
      case command =>
        printResponse(controller.processCommand(command))
        mainLoop
    }
  }

  def getConsoleInput: String = scala.io.StdIn.readLine()

  def printResponse(resp: Response): Unit = {
    println(resp.show)
  }

  def printHelp: Unit = {
    println(
      """
        |
        |
        |
        |
      """.stripMargin
    )
  }

}
