package elevatorsystem.actor

import model.Messages._
import model._

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Success}

import java.time.Duration

/**
  * The Elevator Manager Actor is the actor that interacts with the outside of the actor system.
  * This Actor is in charge of creating new elevators, getting their statuses and assigning requests to them.
  */

class ElevatorManager(conf: ElevatorSystemConfig) extends Actor {

  val elevators: Map[Int, ActorRef] = (0 until conf.elevatorCount).map(id => (id, createElevator(id))).toMap

  implicit val timeout = Timeout(3 seconds)
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case GetSystemStatus => retrieveSystemStatus.pipeTo(sender())

    case request: CallRequest =>
      findSuitableElevator(request).map(x => CallResponse(x, request.floor, request.direction))
        .pipeTo(sender())
        .andThen { case Success(r) => elevators(r.id.id) ! request}

    case request@LandRequest(elevatorId, floor) =>
      elevators(elevatorId.id) ! request
      sender() ! LandResponse(elevatorId, floor)

    case SystemStep =>
      elevators.values.foreach(ref => ref ! ManualStep)
      sender() ! SystemStepResponse()
  }

  /**
    * Retrieve status of all elevators in the system.
    */

  def retrieveSystemStatus: Future[SystemStatus] =
    Future.sequence(elevators.values.toSeq.map(ref => (ref ? GetStatus).mapTo[ElevatorStatus])).map(SystemStatus)

  /**
    * All the elevators are sorted according to their score. The ones that are most suitable are those with the highest
    * score. In the case when there is a group of elevators with the same score we chose the one with the least amount
    * of requests received.
    */

  def findSuitableElevator(request: CallRequest): Future[ElevatorId] =
    retrieveSuitabilityScores(request.floor, request.direction)
      .map(_.sortWith((x, y) => (x.score < y.score) || (x.score == y.score && x.requestCount > y.requestCount)).last.id)

  /**
    * The "figure of suitability" score is calculated by the elevator actors and sent to the manager.
    */

  def retrieveSuitabilityScores(floor: Floor, direction: Direction): Future[Seq[Score]] =
    Future.sequence(elevators.values.toSeq.map(ref => (ref ? GetScore(floor, direction)).mapTo[Score]))

  /**
    * Function to create a new elevator.
    */

  def createElevator(id: Int): ActorRef =
    context.actorOf(Props(new Elevator(ElevatorConfig(ElevatorId(id), conf.floorCount,
      Duration.ofSeconds(conf.travelDuration), Duration.ofSeconds(conf.waitingDuration), conf.manualStepping))))

}
