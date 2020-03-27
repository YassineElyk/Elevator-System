package elevatorsystem.actor

import elevatorsystem.ElevatorStateTransitions
import model.Messages._
import model.TimerModel._
import model._

import akka.actor.{Actor, Timers}

/**
  * This Actor represents an elevator
  */

class Elevator(override val conf: ElevatorConfig) extends Actor with Timers with ElevatorStateTransitions {

  override def receive: Receive = Idle(NoRequest(Floor(0)))

  /**
    * This receiving method represents the Idle state where the elevator either hasn't received any requests yet or has
    * already responded to all the requests it received.
    */

  def Idle(state: NoRequest): Receive = {
    case GetStatus => sender ! ElevatorStatus(conf.id, state)

    case GetScore(floor, direction) => sender ! calculateSuitabilityScore(state, floor, direction, conf.floorCount, conf.id)

    case request: FloorRequest =>
      setNextFloorTimer
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))

    case ManualStep => Unit
  }

  /**
    * This receiving method represents the active state of the elevator meaning when it is either moving down or up.
    */

  def Active(state: Moving): Receive = {
    case GetStatus => sender ! ElevatorStatus(conf.id, state)

    case GetScore(floor, direction) => sender ! calculateSuitabilityScore(state, floor, direction, conf.floorCount, conf.id)

    case request: FloorRequest => context.become(Active(state.processRequest(request).asInstanceOf[Moving]))

    case NextFloorReached|ManualStep =>
      state.next match {
        case newState: Waiting =>
          setWaitingTimer
          context.become(Waiting(newState))
        case newState: Moving =>
          setNextFloorTimer
          context.become(Active(newState))
      }
  }

  /**
    * This receiving method represents the waiting state during which the elevator loads/unloads passengers.
    */

  def Waiting(state: Waiting): Receive = {
    case GetStatus => sender ! ElevatorStatus(conf.id, state)

    case GetScore(floor, direction) => sender ! calculateSuitabilityScore(state, floor, direction, conf.floorCount, conf.id)

    case request: FloorRequest => context.become(Waiting(state.processRequest(request).asInstanceOf[Waiting]))

    case WaitingCompleted|ManualStep =>
      state.next match {
        case newState: Moving =>
          setNextFloorTimer
          context.become(Active(newState))
        case newState: NoRequest =>
          context.become(Idle(newState))
      }
  }

  /**
    * When manual stepping isn't activated, the scheduled messages allow the system to switch between different states as
    * new requests come in so it can evolve independently over time.
    */

  def setNextFloorTimer: Unit = if (!conf.manualStepping) timers.startSingleTimer(MovingToNextFloor, NextFloorReached, conf.travelDuration) else Unit

  def setWaitingTimer: Unit = if (!conf.manualStepping) timers.startSingleTimer(WaitingAtDestination, WaitingCompleted, conf.responseDuration) else Unit

  /**
    * In order to assign a call request to an elevator we calculate a simple cost function known as "figure of suitability".
    */

  def calculateSuitabilityScore(state: ElevatorState, floor: Floor, direction: Direction, fc: Int, id: ElevatorId): Score = {
    state match {
      case s: NoRequest =>
        val d = (s.currentFloor.num - floor.num).abs
        Score(conf.id, fc + 1 - d, 0)
      case s: Moving if (floor.num - s.previousFloor.num+1) * toInt(direction) < 0 =>
        Score(id, 1, s.destinations.size)
      case s: Moving if ((floor.num - s.previousFloor.num+1) * toInt(direction) >= 0) && (s.direction == direction) =>
        val d = (s.previousFloor.num+1 - floor.num).abs
        Score(id, fc + 2 - d, s.destinations.size)
      case s: Moving if ((floor.num - s.previousFloor.num+1) * toInt(direction) >= 0) && (s.direction != direction) =>
        val d = (s.previousFloor.num+1 - floor.num).abs
        Score(id, fc + 1 - d, s.destinations.size)
      case s: Waiting => calculateSuitabilityScore(s.next, floor, direction, fc, id)
    }
  }

  def toInt(dir: Direction): Int = if (dir == Up) 1 else -1

}
