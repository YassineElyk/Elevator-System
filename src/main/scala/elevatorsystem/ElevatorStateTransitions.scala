package elevatorsystem

import elevatorsystem.model.Messages.ElevatorRequest
import elevatorsystem.model.{Down, ElevatorConfig, ElevatorState, Floor, Moving, NoRequest, ReceivedRequestDirection, ReceivedRequestsProperties, Up, Waiting}

import cats.Monoid


trait ElevatorStateTransitions extends StateTransitionImplicit with ReceivedRequestsProperties{

  def conf: ElevatorConfig

  implicit val NoRequestStateTransitions = new ElevatorTransition[NoRequest] {
    override def processRequest(request: ElevatorRequest)(state: NoRequest): ElevatorState = {
      if (request.floor.num < state.currentFloor.num) {
        Moving(
          direction = Down(),
          previousFloor = state.currentFloor,
          lastDestinationUp = None,
          lastDestinationDown = Some(request.floor),
          destinations = addReceivedRequest(request, state.destinations)
        )
      }
      else if (request.floor.num > state.currentFloor.num) {
        model.Moving(
          direction = Up(),
          previousFloor = state.currentFloor,
          lastDestinationUp = Some(request.floor),
          lastDestinationDown = None,
          destinations = addReceivedRequest(request, state.destinations)
        )
      }
      else state
    }

    override def next(state: NoRequest): NoRequest = state
  }

  implicit val MovingStateTransitions = new ElevatorTransition[Moving] {
    override def processRequest(request: ElevatorRequest)(state: Moving): ElevatorState = {
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
        case Up() => Floor(state.previousFloor.num + 1)
        case Down() => Floor(state.previousFloor.num - 1)
      }

      val requestForNextFloor = state.destinations.getOrElse(currentFloor, Monoid[ReceivedRequestDirection].empty)

      if ((state.direction == Up()) && requestForNextFloor.up || (state.direction == Down()) && requestForNextFloor.down ||
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
      else if ((state.direction == Up() && !state.lastDestinationDown.isDefined) && requestForNextFloor != Monoid[ReceivedRequestDirection].empty){
        state.copy(previousFloor = currentFloor, lastDestinationDown = Some(currentFloor))
      }
      else if ((state.direction == Down() && !state.lastDestinationUp.isDefined) && requestForNextFloor != Monoid[ReceivedRequestDirection].empty){
        state.copy(previousFloor = currentFloor, lastDestinationUp = Some(currentFloor))
      }
      else
        state.copy(previousFloor = currentFloor)

    }
  }

  implicit val WaitingStateTransitions = new ElevatorTransition[Waiting] {
    override def processRequest(request: ElevatorRequest)(state: Waiting): ElevatorState = {
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

        case Up() => state.lastDestinationUp match {
          case None => model.NoRequest(state.currentFloor, destinations = initReceivedRequests(conf.floorCount))
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationDown match {
              case None => NoRequest(state.currentFloor, destinations = initReceivedRequests(conf.floorCount))
              case Some(floor) =>
                Moving(
                  direction = Down(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = None,
                  lastDestinationDown = state.lastDestinationDown,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            Moving(
              direction = Up(),
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            )
        }

        case Down() => state.lastDestinationDown match {
          case None => model.NoRequest(state.currentFloor, destinations = initReceivedRequests(conf.floorCount))
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationUp match {
              case None => model.NoRequest(state.currentFloor, destinations = initReceivedRequests(conf.floorCount))
              case Some(floor) =>
                Moving(
                  direction = Up(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = state.lastDestinationUp,
                  lastDestinationDown = None,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            Moving(
              direction = Up(),
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
