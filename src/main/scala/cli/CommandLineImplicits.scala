package cli

import model.Messages._

import cats.Show
import cats.syntax.show._


trait CommandLineImplicits {
/*
  implicit val printabletype: Show[type] = ???
   foldmap on the map of destinations to create a big string that shows all the relevant ones

  Show[cliCommandResponses]
  to show the command responses on the screen for example request/call received/ assigned to elevator Id
*/

  implicit val ResponseShow = Show.show[Response] {
    case r: SystemStatus => r.s
    case r: CallResponse => ""
    case r: LandResponse => ""
  }

}
