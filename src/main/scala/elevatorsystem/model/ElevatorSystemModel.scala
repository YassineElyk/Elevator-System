package elevatorsystem.model

import cats.Monoid
import elevatorsystem.model.RequestModel._


sealed trait ElevatorState

case class NoDestinations(
                           currentFloor: Floor,
                           destinations: Map[Floor, RequestDirection]
                         ) extends ElevatorState

case class Moving(
                   direction: Direction,
                   previousFloor: Floor,
                   lastDestinationUp: Option[Floor],
                   lastDestinationDown: Option[Floor],
                   destinations: Map[Floor, RequestDirection]
                 ) extends ElevatorState

case class Waiting(
                    previousDirection: Direction,
                    currentFloor: Floor,
                    lastDestinationUp: Option[Floor],
                    lastDestinationDown: Option[Floor],
                    destinations: Map[Floor, RequestDirection]
                  ) extends ElevatorState


sealed trait Direction
case class Up() extends Direction
case class Down() extends Direction
case class NoDirection() extends Direction

case class ElevatorConfig(
                           id: ElevatorId,
                           floorCount: Int,
                           travelInterval: java.time.Duration,
                           unloadingInterval: java.time.Duration,
                           manualStepping: Boolean = false
                         )

case class ElevatorId(id: Int) extends AnyVal

object TimerModel {

  case class WaitingAtDestination()

  case class WaitingCompleted()

  case class MovingToNextFloor()

  case class NextFloorReached()

}


case class Floor(num: Int) extends AnyVal

object RequestModel{

  sealed trait ElevatorRequest { def floor: Floor }
  case class PickupRequest(floor :Floor, direction: Direction) extends ElevatorRequest
  case class FloorRequest(floor :Floor) extends ElevatorRequest

  case class RequestDirection(floorRequest: Boolean, up: Boolean, down: Boolean)

  implicit val RequestDirectionMonoid: Monoid[RequestDirection] = new Monoid[RequestDirection] {
    def combine(a: RequestDirection, b: RequestDirection): RequestDirection =
      RequestDirection(a.floorRequest || b.floorRequest, a.up || b.up, a.down || b.down)
    def empty = RequestDirection(false, false, false)
  }

}


sealed trait statusMessage
case class getStatus() extends statusMessage
case class ElevatorStatus(state: ElevatorState) extends statusMessage
