package elevatorsystem.model

import java.time.Duration

case class ElevatorSystemConfig(elevatorCount: Int, floorCount: Int, travelDuration: Duration, responseDuration: Duration)

case object GetSystemStatus
