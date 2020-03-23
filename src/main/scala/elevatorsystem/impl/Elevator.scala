package elevatorsystem.impl

import elevatorsystem._
import elevatorsystem.TimerModel._
import elevatorsystem.RequestModel._
import akka.actor.{Actor, Timers}
import com.typesafe.scalalogging.LazyLogging


class Elevator(override val conf: ElevatorConfig) extends Actor with Timers with ElevatorStateTransitions with LazyLogging {

  override def receive: Receive = {
    logger.info("Elevator number " + conf.id + " has been initialized and is Idle")
    Idle(NoDestinations(Floor(0), initDestinations))
  }

  def Idle(state: NoDestinations): Receive = {
    case getStatus() => sender ! ElevatorStatus(state)
    case request: ElevatorRequest =>
      logger.info("Elevator number " + conf.id + " received a request to travel")
      setNextFloorTimer
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))
  }

  def Active(state: Moving): Receive = {
    case getStatus() => sender ! ElevatorStatus(state)
    case request: ElevatorRequest =>
      logger.info("Elevator number " + conf.id + " received a request to travel")
      context.become(Active(state.processRequest(request).asInstanceOf[Moving]))
    case step: NextFloorReached =>
      val nextState = state.next
      logger.info("elevator in " + nextState.toString)
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
      logger.info("Elevator number " + conf.id + " received a request to travel")
      context.become(Waiting(state.processRequest(request).asInstanceOf[Waiting]))
    case step: WaitingCompleted =>
      logger.info("Waiting completed")
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
