package elevatorsystem.model

object Messages {

  sealed trait ElevatorRequest {def floor: Floor}
  case class CallRequest(floor :Floor, direction: Direction) extends ElevatorRequest
  case class LandingRequest(floor :Floor) extends ElevatorRequest

  case class ElevatorStatus(id: ElevatorId, state: ElevatorState)

  case object GetStatus

  case object GetSystemStatus

  case class SystemStatus()

}
