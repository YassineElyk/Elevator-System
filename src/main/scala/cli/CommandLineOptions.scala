package cli

import model.ElevatorSystemConfig

import scopt.OptionParser

object CommandLineOption{

  val parser = new OptionParser[ElevatorSystemConfig]("Elevator system simulation") {

    opt[Int]('f', "floorCount").action((count, config) =>
      config.copy(floorCount = count))
    opt[Int]('e', "elevatorCount").action((count, config) =>
      config.copy(elevatorCount = count))
    opt[Int]('t', "travelDuration").action((duration, config) =>
      config.copy(elevatorCount = duration))
    opt[Int]('w', "waitingDuration").action((duration, config) =>
      config.copy(elevatorCount = duration))

  }

  def parse(args: Array[String]): Option[ElevatorSystemConfig] =
    parser.parse(args, ElevatorSystemConfig())

}
