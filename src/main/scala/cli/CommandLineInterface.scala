package cli

import elevatorsystem.ElevatorSystemController

class CommandLineInterface(controller: ElevatorSystemController) {

  def start: Unit = {
    mainLoop
  }

  def mainLoop: Unit = {
    print("Elevator simulation is running")

    getConsoleInput

  Show instances ndirohom felevator system bach cli ijiha nichan string taffichih

  }

  def getConsoleInput: String = scala.io.StdIn.readLine()

}
