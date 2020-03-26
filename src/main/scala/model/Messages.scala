package model

/**
  * The case classes that represent most of the messages used in the system.
  */

object Messages {

  /**
    * Requests to move the elevator to a certain floor and their corresponding responses.
    */

  sealed trait FloorRequest {def floor: Floor}
  case class CallRequest(floor :Floor, direction: Direction) extends FloorRequest
  case class LandRequest(id: ElevatorId, floor :Floor) extends FloorRequest

  /**
    * Requests of systems and single elevator statuses.
    */

  sealed trait StatusRequest
  case object GetStatus extends StatusRequest
  case object GetSystemStatus extends StatusRequest

  /**
    * Responses sent by the manager actor.
    */

  sealed trait ManagerResponse
  case class CallResponse(id: ElevatorId, floor :Floor, direction: Direction) extends ManagerResponse
  case class LandResponse(id: ElevatorId, floor :Floor) extends ManagerResponse
  case class SystemStatus(statuses: Seq[ElevatorStatus]) extends ManagerResponse
  case class SystemStepResponse() extends ManagerResponse
  case class NoResponse() extends ManagerResponse

  /**
    * Responses sent by the elevator Actor.
    */

  sealed trait ElevatorResponse
  case class ElevatorStatus(id: ElevatorId, state: ElevatorState) extends ElevatorResponse
  case class Score(id: ElevatorId, score: Int, requestCount: Int) extends ElevatorResponse

  /**
    * Request to get the suitability score of a particular elevator.
    */

  case class GetScore(floor: Floor, direction: Direction)

  /**
    * Perform a step in the simulation.
    */

  case object SystemStep

}
