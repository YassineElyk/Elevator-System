import java.time.Duration

import elevatorsystem.actor.Elevator
import elevatorsystem.model._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import cats.kernel.Monoid
import elevatorsystem.model.RequestModel.{FloorRequest, PickupRequest, RequestDirection}
import elevatorsystem.model.TimerModel.{NextFloorReached, WaitingCompleted}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers


class ElevatorActorTestSpec extends TestKit(ActorSystem("TestActorSystem")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll {

  val config = ElevatorConfig(ElevatorId(1), 7, Duration.ofSeconds(4), Duration.ofSeconds(6), true)
  val elevator = system.actorOf(Props(new Elevator(config)))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The elevator Actor" must {
    "be correctly initialized" in {
      elevator ! GetStatus()
      expectMsg(
        ElevatorStatus(
          NoDestinations(
            Floor(0),
            Map(Floor(0) -> Monoid[RequestDirection].empty,
              Floor(1) -> Monoid[RequestDirection].empty,
              Floor(2) -> Monoid[RequestDirection].empty,
              Floor(3) -> Monoid[RequestDirection].empty,
              Floor(4) -> Monoid[RequestDirection].empty,
              Floor(5) -> Monoid[RequestDirection].empty,
              Floor(6) -> Monoid[RequestDirection].empty)
          )
        )
      )
    }

    "Transit to Moving state after receiving first Request" in {
      elevator ! PickupRequest(Floor(1), Down())
      elevator ! GetStatus()
      expectMsg(
        ElevatorStatus(
          Moving(Up(), Floor(0), Some(Floor(1)), None,
            Map(Floor(0) -> Monoid[RequestDirection].empty,
              Floor(1) -> RequestDirection(false, false, true),
              Floor(2) -> Monoid[RequestDirection].empty,
              Floor(3) -> Monoid[RequestDirection].empty,
              Floor(4) -> Monoid[RequestDirection].empty,
              Floor(5) -> Monoid[RequestDirection].empty,
              Floor(6) -> Monoid[RequestDirection].empty
            )
          )
        )
      )
    }

    "Ignore floors with requests that have a different direction (Down) as long as a further request is present" in {
      elevator ! PickupRequest(Floor(6), Down())
      elevator ! NextFloorReached()
      elevator ! GetStatus()
      expectMsg(
        ElevatorStatus(
          Moving(Up(), Floor(1), Some(Floor(6)), Some(Floor(1)),
            Map(Floor(0) -> Monoid[RequestDirection].empty,
              Floor(1) -> RequestDirection(false, false, true),
              Floor(2) -> Monoid[RequestDirection].empty,
              Floor(3) -> Monoid[RequestDirection].empty,
              Floor(4) -> Monoid[RequestDirection].empty,
              Floor(5) -> Monoid[RequestDirection].empty,
              Floor(6) -> RequestDirection(false, false, true)
            )
          )
        )
      )
    }

    "Answer requests that have the same direction (Up)" in {
      elevator ! PickupRequest(Floor(2), Up())
      elevator ! NextFloorReached()
      elevator ! GetStatus()
      expectMsg(
        ElevatorStatus(
          Waiting(Up(), Floor(2), Some(Floor(6)), Some(Floor(1)),
            Map(Floor(0) -> Monoid[RequestDirection].empty,
              Floor(1) -> RequestDirection(false, false, true),
              Floor(2) -> Monoid[RequestDirection].empty,
              Floor(3) -> Monoid[RequestDirection].empty,
              Floor(4) -> Monoid[RequestDirection].empty,
              Floor(5) -> Monoid[RequestDirection].empty,
              Floor(6) -> RequestDirection(false, false, true)
            )
          )
        )
      )
    }
  }

  "Store requests that have been made with different directions (Up, Down) in the same floor and answer them" in {
    elevator ! PickupRequest(Floor(3), Down())
    elevator ! PickupRequest(Floor(3), Up())
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Up(), Floor(2), Some(Floor(6)), Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> RequestDirection(false, true, true),
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> RequestDirection(false, false, true)
          )
        )
      )
    )

    elevator ! WaitingCompleted()
    elevator ! NextFloorReached()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Up(), Floor(3), Some(Floor(6)), Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> RequestDirection(false, false, true)
          )
        )
      )
    )

  }

  "Answer floor requests" in {
    elevator ! FloorRequest(Floor(4))
    elevator ! WaitingCompleted()
    elevator ! NextFloorReached()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Up(), Floor(4), Some(Floor(6)), Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> RequestDirection(false, false, true)
          )
        )
      )
    )

  }

  "Respond to requests where a pickup and floor request have been made in the same floor" in {
    elevator ! FloorRequest(Floor(5))
    elevator ! PickupRequest(Floor(5), Down())
    elevator ! WaitingCompleted()
    elevator ! NextFloorReached()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Up(), Floor(5), Some(Floor(6)), Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> RequestDirection(false, false, true)
          )
        )
      )
    )

  }

  "Answer furthest call even if it has an opposite direction" in {
    elevator ! WaitingCompleted()
    elevator ! NextFloorReached()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Up(), Floor(6), Some(Floor(6)), Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> Monoid[RequestDirection].empty
          )
        )
      )
    )

  }

  "Switch directions after responding to the furthest upward call" in {
    elevator ! WaitingCompleted()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Moving(Down(), Floor(6), None, Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> RequestDirection(false, false, true),
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> Monoid[RequestDirection].empty
          )
        )
      )
    )
  }

  "Respond to Requests that have the same direction (Down)" in {
    elevator ! NextFloorReached()
    elevator ! NextFloorReached()
    elevator ! NextFloorReached()
    elevator ! NextFloorReached()
    elevator ! NextFloorReached()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        Waiting(Down(), Floor(1), None, Some(Floor(1)),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> Monoid[RequestDirection].empty,
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> Monoid[RequestDirection].empty
          )
        )
      )
    )

  }

  "Become Idle again since all requests have been answered" in {
    elevator ! WaitingCompleted()
    elevator ! GetStatus()
    expectMsg(
      ElevatorStatus(
        NoDestinations(
          Floor(1),
          Map(Floor(0) -> Monoid[RequestDirection].empty,
            Floor(1) -> Monoid[RequestDirection].empty,
            Floor(2) -> Monoid[RequestDirection].empty,
            Floor(3) -> Monoid[RequestDirection].empty,
            Floor(4) -> Monoid[RequestDirection].empty,
            Floor(5) -> Monoid[RequestDirection].empty,
            Floor(6) -> Monoid[RequestDirection].empty)
        )
      )
    )

  }


}
