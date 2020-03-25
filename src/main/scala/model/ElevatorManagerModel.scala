package model

import java.time.Duration

case class ElevatorSystemConfig(
                                 elevatorCount: Int = 2,
                                 floorCount: Int = 10,
                                 travelDuration: Duration = Duration.ofSeconds(30),
                                 waitingDuration: Duration = Duration.ofSeconds(10)
                               )
