package model

/**
  * Global elevator system configuration.
  */

case class ElevatorSystemConfig(
                                 elevatorCount: Int = 2,
                                 floorCount: Int = 10,
                                 travelDuration: Int = 30,
                                 waitingDuration: Int = 10,
                                 manualStepping: Boolean = false
                               )
