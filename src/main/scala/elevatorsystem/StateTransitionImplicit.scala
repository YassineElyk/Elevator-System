package elevatorsystem

import model.ElevatorState
import model.Messages.FloorRequest

trait StateTransitionImplicit extends StateTransition {

  implicit class ElevatorTransitionHelper[S <: ElevatorState](from: S) {
    def processRequest(r: FloorRequest)(implicit t: ElevatorTransition[S]): ElevatorState = t.processRequest(r)(from)
    def next(implicit t: ElevatorTransition[S]): ElevatorState = t.next(from)
  }

}
