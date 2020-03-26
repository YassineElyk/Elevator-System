package elevatorsystem

import model.ElevatorState
import model.Messages.FloorRequest


trait  StateTransition {

  /**
    * A helper type class to represent state transitions between different elevator states.
    */

  trait ElevatorTransition[A <: ElevatorState] {
    def processRequest(request: FloorRequest)(state: A): ElevatorState
    def next(state: A): ElevatorState
  }

}

