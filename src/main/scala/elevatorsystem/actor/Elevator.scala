package elevatorsystem.actor

import akka.actor.{Actor, Timers}
import elevatorsystem.ElevatorStateTransitions
import elevatorsystem.model.Messages._
import elevatorsystem.model.TimerModel._
import elevatorsystem.model._


class Elevator(override val conf: ElevatorConfig) extends Actor with Timers with ElevatorStateTransitions {

  override def receive: Receive = Idle(NoRequest(Floor(0), initReceivedRequests(conf.floorCount)))

  def Idle(state: NoRequest): Receive = {
    case GetStatus => sender ! ElevatorStatus(conf.id, state)
    case request: ElevatorRequest =>
      setNextFloorTimer
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))
  }

  def Active(state: Moving): Receive = {
    case GetStatus => sender ! ElevatorStatus(conf.id, state)
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
    case GetStatus => sender ! ElevatorStatus(conf.id, state)
    case request: ElevatorRequest =>
      context.become(Waiting(state.processRequest(request).asInstanceOf[Waiting]))
    case step: WaitingCompleted =>
      val nextState = state.next
      nextState match {
        case s: Moving =>
          setNextFloorTimer
          context.become(Active(nextState.asInstanceOf[Moving]))
        case s: NoRequest =>
          context.become(Idle(nextState.asInstanceOf[NoRequest]))
      }
  }


  def setNextFloorTimer: Unit = if (conf.manualStepping) timers.startSingleTimer(MovingToNextFloor, NextFloorReached, conf.travelDuration)

  def setWaitingTimer: Unit = if (conf.manualStepping) timers.startSingleTimer(WaitingAtDestination, WaitingCompleted, conf.responseDuration)

}
