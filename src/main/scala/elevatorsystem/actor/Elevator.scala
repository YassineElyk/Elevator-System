package elevatorsystem.actor

import akka.actor.{Actor, Timers}
import elevatorsystem.ElevatorStateTransitions
import elevatorsystem.model.RequestModel._
import elevatorsystem.model.TimerModel._
import elevatorsystem.model._


class Elevator(override val conf: ElevatorConfig) extends Actor with Timers with ElevatorStateTransitions {

  override def receive: Receive = Idle(NoDestinations(Floor(0), initDestinations))

  def Idle(state: NoDestinations): Receive = {
    case getStatus() => sender ! ElevatorStatus(state)
    case request: ElevatorRequest =>
      setNextFloorTimer
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))
  }

  def Active(state: Moving): Receive = {
    case getStatus() => sender ! ElevatorStatus(state)
    case request: ElevatorRequest =>
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))
    case step: NextFloorReached =>
      val nextState = state.next
      nextState match {
        case s: Waiting =>
          setWaitingTimer
          context.become(Waiting(nextState.asInstanceOf[Waiting]))
        case _ => context.become(Active(nextState.asInstanceOf[Moving]))
      }
  }

  def Waiting(state: Waiting): Receive = {
    case getStatus() => sender ! ElevatorStatus(state)
    case request: ElevatorRequest =>
      context.become(Waiting(state.processRequest(request).asInstanceOf[Waiting]))
    case step: WaitingCompleted =>
      val nextState = state.next
      nextState match {
        case s: Moving =>
          setNextFloorTimer
          context.become(Active(nextState.asInstanceOf[Moving]))
        case s: NoDestinations =>
          context.become(Idle(nextState.asInstanceOf[NoDestinations]))
      }
  }


  def setNextFloorTimer: Unit = if (conf.manualStepping) timers.startSingleTimer(MovingToNextFloor, NextFloorReached, conf.travelInterval)

  def setWaitingTimer: Unit = if (conf.manualStepping) timers.startSingleTimer(WaitingAtDestination, WaitingCompleted, conf.unloadingInterval)

}
