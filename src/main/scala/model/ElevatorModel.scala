package model

/**
  * Sealed trait of all the elevator states possible.
  */

sealed trait ElevatorState

case class NoRequest(currentFloor: Floor) extends ElevatorState

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

/**
  * A representation of elevator Id's.
  */

case class ElevatorId(id: Int) extends AnyVal


/**
  * A representation of a system step message received by the elevator actor.
  */

case object ManualStep

/**
  * Messages scheduled by Akka's timer to trigger state transitions.
  */

object TimerModel {

  case object WaitingAtDestination

  case object WaitingCompleted

  case object MovingToNextFloor

  case object NextFloorReached

}

/**
  * A representation of a floor in the system.
  */

case class Floor(num: Int) extends AnyVal

/**
  * A representation of possible directions a Call or Land request can have.
  */

sealed trait Direction
case object Up extends Direction
case object Down extends Direction
case object NoDirection extends Direction

