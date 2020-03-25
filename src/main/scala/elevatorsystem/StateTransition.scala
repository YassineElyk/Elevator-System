package elevatorsystem

import model.ElevatorState
import model.Messages.ElevatorRequest


trait StateTransition {

  trait ElevatorTransition[A <: ElevatorState] {
    def processRequest(request: ElevatorRequest)(state: A): ElevatorState
    def next(state: A): ElevatorState
  }

}

