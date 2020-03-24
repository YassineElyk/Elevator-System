package elevatorsystem

import elevatorsystem.model.ElevatorState
import elevatorsystem.model.Messages.ElevatorRequest


trait StateTransition {

  trait ElevatorTransition[A <: ElevatorState] {
    def processRequest(request: ElevatorRequest)(state: A): ElevatorState
    def next(state: A): ElevatorState
  }

}

