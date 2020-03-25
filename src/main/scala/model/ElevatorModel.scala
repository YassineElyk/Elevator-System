package model

sealed trait ElevatorState

case class NoRequest(
                      currentFloor: Floor,
                      destinations: Map[Floor, ReceivedRequestDirection]
                    ) extends ElevatorState

case class Moving(
                   direction: Direction,
                   previousFloor: Floor,
                   lastDestinationUp: Option[Floor],
                   lastDestinationDown: Option[Floor],
                   destinations: Map[Floor, ReceivedRequestDirection]
                 ) extends ElevatorState

case class Waiting(
                    previousDirection: Direction,
                    currentFloor: Floor,
                    lastDestinationUp: Option[Floor],
                    lastDestinationDown: Option[Floor],
                    destinations: Map[Floor, ReceivedRequestDirection]
                  ) extends ElevatorState

case class ElevatorConfig(
                           id: ElevatorId,
                           floorCount: Int,
                           travelDuration: java.time.Duration,
                           responseDuration: java.time.Duration,
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

sealed trait Direction
case class Up() extends Direction
case class Down() extends Direction
case class NoDirection() extends Direction

