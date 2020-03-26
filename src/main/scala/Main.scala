import cli.{CommandLineInterface, CommandLineOption}
import elevatorsystem.ElevatorSystemController
import model.ElevatorSystemConfig

object Main extends App {

  CommandLineOption.parse(args) match {
    case Some(config) => startApp(config)
    case None =>
  }

  def startApp(config: ElevatorSystemConfig): Unit = {
    val controller: ElevatorSystemController = new ElevatorSystemController(config)
    val cli: CommandLineInterface = new CommandLineInterface(controller)

    cli.start
  }

}
