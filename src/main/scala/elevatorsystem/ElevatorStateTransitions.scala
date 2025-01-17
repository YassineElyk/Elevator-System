package elevatorsystem

import model.Messages.FloorRequest
import cats.Monoid
import model.{Down, ElevatorConfig, ElevatorState, Floor, Moving, NoRequest, ReceivedRequestDirection, ReceivedRequestsProperties, Up, Waiting}

/**
  * These type class instances are helpers to represent elevator state transitions.
  */

trait ElevatorStateTransitions extends StateTransitionImplicit with ReceivedRequestsProperties{

  def conf: ElevatorConfig

  /**
    * Possible state transitions when the elevator is in Idle state and that happen when receiving a new request.
    */

  implicit val NoRequestStateTransitions = new ElevatorTransition[NoRequest] {
    override def processRequest(request: FloorRequest)(state: NoRequest): ElevatorState = {
      if (request.floor.num < state.currentFloor.num) {
        Moving(
          direction = Down,
          previousFloor = state.currentFloor,
          lastDestinationUp = None,
          lastDestinationDown = Some(request.floor),
          destinations = addReceivedRequest(request)
        )
      }
      else if (request.floor.num > state.currentFloor.num) {
        model.Moving(
          direction = Up,
          previousFloor = state.currentFloor,
          lastDestinationUp = Some(request.floor),
          lastDestinationDown = None,
          destinations = addReceivedRequest(request)
        )
      }
      else state
    }

    override def next(state: NoRequest): NoRequest = state
  }

  /**
    * Possible state transitions when the elevator is moving and that happen when new requests or scheduled timer
    * messages have been received.
    */


  implicit val MovingStateTransitions = new ElevatorTransition[Moving] {
    override def processRequest(request: FloorRequest)(state: Moving): ElevatorState = {
      if (state.lastDestinationUp.isDefined && request.floor.num > state.lastDestinationUp.get.num) {
        state.copy(lastDestinationUp = Some(request.floor), destinations = addReceivedRequest(request, state.destinations))
      }
      else if (state.lastDestinationDown.isDefined && request.floor.num < state.lastDestinationDown.get.num) {
        state.copy(lastDestinationDown = Some(request.floor), destinations = addReceivedRequest(request, state.destinations))
      }
      else{
        state.copy(destinations = addReceivedRequest(request, state.destinations))
      }
    }

    override def next(state: Moving): ElevatorState = {
      val currentFloor = state.direction match {
        case Up => Floor(state.previousFloor.num + 1)
        case Down => Floor(state.previousFloor.num - 1)
      }

      val requestForNextFloor = state.destinations.getOrElse(currentFloor, Monoid[ReceivedRequestDirection].empty)

      if ((state.direction == Up && requestForNextFloor.up) || (state.direction == Down) && requestForNextFloor.down ||
        (requestForNextFloor.floorRequest) || (currentFloor == state.lastDestinationUp.getOrElse(Floor(-1))) ||
        (currentFloor == state.lastDestinationDown.getOrElse(Floor(-1)))){

        val newDestinations = removeReceivedRequest(currentFloor, state.destinations)

        Waiting(
          previousDirection = state.direction,
          currentFloor = currentFloor,
          lastDestinationUp = state.lastDestinationUp,
          lastDestinationDown = state.lastDestinationDown,
          destinations = newDestinations
        )
      }
      else if ((state.direction == Up && !state.lastDestinationDown.isDefined) && requestForNextFloor != Monoid[ReceivedRequestDirection].empty){
        state.copy(previousFloor = currentFloor, lastDestinationDown = Some(currentFloor))
      }
      else if ((state.direction == Down && !state.lastDestinationUp.isDefined) && requestForNextFloor != Monoid[ReceivedRequestDirection].empty){
        state.copy(previousFloor = currentFloor, lastDestinationUp = Some(currentFloor))
      }
      else
        state.copy(previousFloor = currentFloor)

    }
  }

  /**
    * Possible transitions from the waiting state of the elevator.
    */


  implicit val WaitingStateTransitions = new ElevatorTransition[Waiting] {
    override def processRequest(request: FloorRequest)(state: Waiting): ElevatorState = {
      if (state.currentFloor.num == request.floor.num) state
      else if (state.lastDestinationUp.isDefined && request.floor.num > state.lastDestinationUp.get.num) {
        state.copy(lastDestinationUp = Some(request.floor), destinations = addReceivedRequest(request, state.destinations))
      }
      else if (state.lastDestinationDown.isDefined && request.floor.num < state.lastDestinationDown.get.num) {
        state.copy(lastDestinationDown = Some(request.floor), destinations = addReceivedRequest(request, state.destinations))
      }
      else {
        state.copy(destinations = addReceivedRequest(request, state.destinations))
      }
    }

    override def next(state: Waiting): ElevatorState = {

      state.previousDirection match {

        case Up => state.lastDestinationUp match {
          case None => model.NoRequest(state.currentFloor)
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationDown match {
              case None => NoRequest(state.currentFloor)
              case Some(floor) =>
                Moving(
                  direction = Down,
                  previousFloor = state.currentFloor,
                  lastDestinationUp = None,
                  lastDestinationDown = state.lastDestinationDown,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            Moving(
              direction = Up,
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            )
        }

        case Down => state.lastDestinationDown match {
          case None => model.NoRequest(state.currentFloor)
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationUp match {
              case None => model.NoRequest(state.currentFloor)
              case Some(floor) =>
                Moving(
                  direction = Up,
                  previousFloor = state.currentFloor,
                  lastDestinationUp = state.lastDestinationUp,
                  lastDestinationDown = None,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            Moving(
              direction = Up,
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            )
        }
      }
    }
  }

}
