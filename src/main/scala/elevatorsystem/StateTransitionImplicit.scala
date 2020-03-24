package elevatorsystem

import elevatorsystem.model.ElevatorState
import elevatorsystem.model.Messages.ElevatorRequest

trait StateTransitionImplicit extends StateTransition {
  implicit class ElevatorTransitionHelper[S <: ElevatorState](from: S) {
    def processRequest(r: ElevatorRequest)(implicit t: ElevatorTransition[S]): ElevatorState = t.processRequest(r)(from)
    def next(implicit t: ElevatorTransition[S]): ElevatorState = t.next(from)
  }
}
