package elevatorsystem

import elevatorsystem.RequestModel._
import cats.Monoid
import cats.syntax.semigroup._
import com.typesafe.scalalogging.LazyLogging


trait ElevatorStateTransitions extends StateTransitionImplicit with LazyLogging {

  def conf: ElevatorConfig

  implicit val NoDestionationsStateTransitions = new ElevatorTransition[NoDestinations] {
    override def processRequest(request: ElevatorRequest)(state: NoDestinations): ElevatorState = {
      if (request.floor.num < state.currentFloor.num) {

        val s = Moving(
          direction = Down(),
          previousFloor = state.currentFloor,
          lastDestinationUp = None,
          lastDestinationDown = Some(request.floor),
          destinations = addDestination(request, state.destinations)
        )
        logger.info(s.toString)

        Moving(
          direction = Down(),
          previousFloor = state.currentFloor,
          lastDestinationUp = None,
          lastDestinationDown = Some(request.floor),
          destinations = addDestination(request, state.destinations)
        )
      }
      else if (request.floor.num > state.currentFloor.num) {
        val s = Moving(
          direction = Up(),
          previousFloor = state.currentFloor,
          lastDestinationUp = Some(request.floor),
          lastDestinationDown = None,
          destinations = addDestination(request, state.destinations)
        )
        logger.info(s.toString)

        Moving(
          direction = Up(),
          previousFloor = state.currentFloor,
          lastDestinationUp = Some(request.floor),
          lastDestinationDown = None,
          destinations = addDestination(request, state.destinations)
        )
      }
      else {
        logger.info(state.toString)
        state
      }
    }

    override def next(state: NoDestinations): NoDestinations = {
      logger.info(state.toString)
      state}
  }

  implicit val MovingStateTransitions = new ElevatorTransition[Moving] {
    override def processRequest(request: ElevatorRequest)(state: Moving): ElevatorState = {
      if (state.lastDestinationUp.isDefined && request.floor.num > state.lastDestinationUp.get.num) {
        state.copy(lastDestinationUp = Some(request.floor),destinations = addDestination(request, state.destinations))
      }
      else if (state.lastDestinationDown.isDefined && request.floor.num < state.lastDestinationDown.get.num) {
        state.copy(lastDestinationDown = Some(request.floor) ,destinations = addDestination(request, state.destinations))
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

      //if currentFloor is on same direction or a last remove it from destinations and wait in that floor.
      if ((state.direction == Up()) && state.destinations.getOrElse(currentFloor, Monoid[RequestDirection].empty).up ||
        (state.direction == Down()) && state.destinations.getOrElse(currentFloor, Monoid[RequestDirection].empty).down ||
        (currentFloor == state.lastDestinationUp.getOrElse(Floor(-1))) || (currentFloor == state.lastDestinationDown.getOrElse(Floor(-1)))){

        val newDestinations = removeDestination(currentFloor, state.destinations)
        val s = Waiting(
          previousDirection = state.direction,
          currentFloor = currentFloor,
          lastDestinationUp = state.lastDestinationUp,
          lastDestinationDown = state.lastDestinationDown,
          destinations = newDestinations
        )
        logger.info(s.toString)
        Waiting(
          previousDirection = state.direction,
          currentFloor = currentFloor,
          lastDestinationUp = state.lastDestinationUp,
          lastDestinationDown = state.lastDestinationDown,
          destinations = newDestinations
        )
      }
      else
      logger.info(state.copy(previousFloor = currentFloor).toString)
        state.copy(previousFloor = currentFloor)

    }
  }

  implicit val WaitingStateTransitions = new ElevatorTransition[Waiting] {
    override def processRequest(request: ElevatorRequest)(state: Waiting): ElevatorState = {
      if (state.lastDestinationUp.isDefined && request.floor.num > state.lastDestinationUp.get.num) {
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
          case None => {
            val s = NoDestinations(state.currentFloor, destinations = initDestinations)
            logger.info(s.toString)
            NoDestinations(state.currentFloor, destinations = initDestinations)}
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationDown match {
              case None => {
                logger.info( NoDestinations(state.currentFloor, destinations = initDestinations).toString)
                NoDestinations(state.currentFloor, destinations = initDestinations)}
              case Some(floor) =>
                logger.info(Moving(
                  direction = Up(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = None,
                  lastDestinationDown = state.lastDestinationDown,
                  destinations = state.destinations
                ).toString)
                Moving(
                  direction = Up(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = None,
                  lastDestinationDown = state.lastDestinationDown,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            logger.info(Moving(
              direction = Up(),
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            ).toString)
            Moving(
              direction = Up(),
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            )
        }

        case Down() => state.lastDestinationDown match {
          case None => {
            logger.info(NoDestinations(state.currentFloor, destinations = initDestinations).toString)
            NoDestinations(state.currentFloor, destinations = initDestinations)}
          case Some(floor) if state.currentFloor == floor =>

            state.lastDestinationUp match {
              case None => {logger.info(NoDestinations(state.currentFloor, destinations = initDestinations).toString);NoDestinations(state.currentFloor, destinations = initDestinations)}
              case Some(floor) =>
                logger.info(                Moving(
                  direction = Up(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = state.lastDestinationUp,
                  lastDestinationDown = None,
                  destinations = state.destinations
                )
                .toString)
                Moving(
                  direction = Up(),
                  previousFloor = state.currentFloor,
                  lastDestinationUp = state.lastDestinationUp,
                  lastDestinationDown = None,
                  destinations = state.destinations
                )
            }

          case Some(floor) if state.currentFloor != floor =>
            logger.info(            Moving(
              direction = Up(),
              previousFloor = state.currentFloor,
              lastDestinationUp = state.lastDestinationUp,
              lastDestinationDown = state.lastDestinationDown,
              destinations = state.destinations
            )
            .toString)
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
    destinations + (floor -> RequestDirection(false, false, false))

  def initDestinations: Map[Floor, RequestDirection] =
    (0 until conf.floorCount).map(x => (Floor(x), Monoid[RequestDirection].empty)).toMap

}
