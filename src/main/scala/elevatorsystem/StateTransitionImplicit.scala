package elevatorsystem

import model.ElevatorState
import model.Messages.FloorRequest

trait StateTransitionImplicit extends StateTransition {

  /**
    * A helper implicit class to associate the state transition methods with the relevant data types.
    */

  implicit class ElevatorTransitionHelper[S <: ElevatorState](from: S) {
    def processRequest(r: FloorRequest)(implicit t: ElevatorTransition[S]): ElevatorState = t.processRequest(r)(from)
    def next(implicit t: ElevatorTransition[S]): ElevatorState = t.next(from)
  }

}
