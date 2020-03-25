package elevatorsystem

import model.ElevatorState
import model.Messages.FloorRequest


trait StateTransition {

  trait ElevatorTransition[A <: ElevatorState] {
    def processRequest(request: FloorRequest)(state: A): ElevatorState
    def next(state: A): ElevatorState
  }

}

