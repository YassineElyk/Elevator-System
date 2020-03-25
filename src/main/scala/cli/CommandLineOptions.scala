package cli

import model.ElevatorSystemConfig


object CommandLineOption{

  val parser = new scopt.OptionParser[ElevatorSystemConfig]("Elevator system simulation") {

    opt[Int]('f', "floorCount").required().action((count, config) =>
      config.copy(floorCount = count)).text("floorCount is an integer property")
    opt[Int]('e', "elevatorCount").required().action((count, config) =>
      config.copy(elevatorCount = count)).text("elevatorCount is an integer property")
    opt[Int]('t', "travelDuration").required().action((duration, config) =>
      config.copy(elevatorCount = duration)).text("elevatorCount is an integer property")
    opt[Int]('w', "waitingDuration").required().action((duration, config) =>
      config.copy(elevatorCount = duration)).text("elevatorCount is an integer property")

  }

  def parse(args: Array[String]): Option[ElevatorSystemConfig] =
    parser.parse(args, ElevatorSystemConfig())

}
