package elevatorsystem

import elevatorsystem.model.RequestModel._
import cats.Monoid
import cats.syntax.semigroup._
import elevatorsystem.model.{Down, ElevatorConfig, ElevatorState, Floor, Moving, NoDestinations, Up, Waiting}


trait ElevatorStateTransitions extends StateTransitionImplicit {

  def conf: ElevatorConfig

  implicit val NoDestionationsStateTransitions = new ElevatorTransition[NoDestinations] {
    override def processRequest(request: ElevatorRequest)(state: NoDestinations): ElevatorState = {
      if (request.floor.num < state.currentFloor.num) {
        Moving(
          direction = Down(),
          previousFloor = state.currentFloor,
          lastDestinationUp = None,
          lastDestinationDown = Some(request.floor),
          destinations = addDestination(request, state.destinations)
        )
      }
      else if (request.floor.num > state.currentFloor.num) {
        model.Moving(
          direction = Up(),
          previousFloor = state.currentFloor,
          lastDestinationUp = Some(request.floor),
          lastDestinationDown = None,
          destinations = addDestination(request, state.destinations)
        )
      }
      else state
    }

    override def next(state: NoDestinations): NoDestinations = state
  }

  implicit val MovingStateTransitions = new ElevatorTransition[Moving] {
    override def processRequest(request: ElevatorRequest)(state: Moving): ElevatorState = {
      if (state.lastDestinationUp.isDefined && request.floor.num > state.lastDestinationUp.get.num) {
        state.copy(lastDestinationUp = Some(request.floor), destinations = addDestination(request, state.destinations))
      }
      else if (state.lastDestinationDown.isDefined && request.floor.num < state.lastDestinationDown.get.num) {
        state.copy(lastDestinationDown = Some(request.floor), destinations = addDestination(request, state.destinations))
      }
      else{
        state.copy(destinations = addDestination(request, state.destinations))
      }
    }

    override def next(state: Moving): ElevatorState = {
      val currentFloor = state.direction match {
        case Up() => Floor(state.previousFloor.num + 1)
        case Down() => Floor(state.previousFloor.num - 1)
      }

      val requestForNextFloor = state.destinations.getOrElse(currentFloor, Monoid[RequestDirection].empty)

      if ((state.direction == Up()) && requestForNextFloor.up || (state.direction == Down()) && requestForNextFloor.down ||
        (requestForNextFloor.floorRequest) || (currentFloor == state.lastDestinationUp.getOrElse(Floor(-1))) ||
        (currentFloor == state.lastDestinationDown.getOrElse(Floor(-1)))){

        val newDestinations = removeDestination(currentFloor, state.destinations)

        Waiting(
          previousDirection = state.direction,
          currentFloor = currentFloor,
          lastDestinationUp = state.lastDestinationUp,
          lastDestinationDown = state.lastDestinationDown,
          destinations = newDestinations
        )
      }
      else if ((state.direction == Up() && !state.lastDestinationDown.isDefined) && requestForNextFloor != Monoid[RequestDirection].empty){
        state.copy(previousFloor = currentFloor, lastDestinationDown = Some(currentFloor))
      }
      else if ((state.direction == Down() && !state.lastDestinationUp.isDefined) && requestForNextFloor != Monoid[RequestDirection].empty){
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
        state.copy(lastDestinationUp = Some(request.floor), destinations = addDestination(request, state.destinations))
      }
      else if (state.lastDestinationDown.isDefined && request.floor.num < state.lastDestinationDown.get.num) {
        state.copy(lastDestinationDown = Some(request.floor), destinations = addDestination(request, state.destinations))
      }
      else {
        state.copy(destinations = addDestination(request, state.destinations))
      }
    }

    override def next(state: Waiting): ElevatorState = {

      state.previousDirection match {

        case Up() => state.lastDestinationUp match {
          case None => model.NoDestinations(state.currentFloor, destinations = initDestinations)
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationDown match {
              case None => NoDestinations(state.currentFloor, destinations = initDestinations)
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
          case None => model.NoDestinations(state.currentFloor, destinations = initDestinations)
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationUp match {
              case None => model.NoDestinations(state.currentFloor, destinations = initDestinations)
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

  def addDestination(request :ElevatorRequest, destinations: Map[Floor, RequestDirection]): Map[Floor, RequestDirection] = {
    val oldRequestDirection = destinations.getOrElse(request.floor, Monoid[RequestDirection].empty) //Shouldn't happen that we dont find an instance in the map
    request match {
      case r: FloorRequest =>
        destinations + (r.floor -> (RequestDirection(true, false, false) |+| oldRequestDirection))
      case r: PickupRequest =>
        if (r.direction == Up()) destinations + (r.floor -> (RequestDirection(false, true, false) |+| oldRequestDirection))
        else destinations + (r.floor -> (RequestDirection(false, false, true) |+| oldRequestDirection))
    }
  }

  def removeDestination(floor: Floor, destinations: Map[Floor, RequestDirection]): Map[Floor, RequestDirection] =
    destinations + (floor -> Monoid[RequestDirection].empty)

  def initDestinations: Map[Floor, RequestDirection] =
    (0 until conf.floorCount).map(x => (Floor(x), Monoid[RequestDirection].empty)).toMap

}
