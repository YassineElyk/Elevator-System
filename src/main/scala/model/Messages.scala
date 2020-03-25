package model

object Messages {

  sealed trait FloorRequest {def floor: Floor}
  case class CallRequest(floor :Floor, direction: Direction) extends FloorRequest
  case class LandRequest(floor :Floor) extends FloorRequest

  sealed trait StatusRequest
  case object GetStatus extends StatusRequest
  case object GetSystemStatus extends StatusRequest

  sealed trait Response
  case class CallResponse(id: ElevatorId) extends Response
  case class LandResponse() extends Response
  case class ElevatorStatus(id: ElevatorId, state: ElevatorState) extends Response
  case class SystemStatus(s: String) extends Response

}
