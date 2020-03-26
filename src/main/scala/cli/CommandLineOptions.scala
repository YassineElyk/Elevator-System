package cli

import model.ElevatorSystemConfig

import scopt.OptionParser

/**
  * A parser for the options to pass when starting the program
  */

object CommandLineOption {

  val parser = new OptionParser[ElevatorSystemConfig]("Elevator system simulation") {

    opt[Int]('f', "floorCount").action((count, config) =>
      config.copy(floorCount = count))
    opt[Int]('e', "elevatorCount").action((count, config) =>
      config.copy(elevatorCount = count))
    opt[Int]('t', "travelDuration").action((duration, config) =>
      config.copy(travelDuration = duration))
    opt[Int]('w', "waitingDuration").action((duration, config) =>
      config.copy(waitingDuration = duration))
    opt[Boolean]('m', "manualStepping").action((bool, config) =>
      config.copy(manualStepping = bool))
  }

  def parse(args: Array[String]): Option[ElevatorSystemConfig] =
    parser.parse(args, ElevatorSystemConfig())

}
