package elevatorsystem

import elevatorsystem.model.ElevatorState
import elevatorsystem.model.RequestModel._


trait StateTransition {

  trait ElevatorTransition[A <: ElevatorState] {
    def processRequest(request: ElevatorRequest)(state: A): ElevatorState
    def next(state: A): ElevatorState
  }

}

