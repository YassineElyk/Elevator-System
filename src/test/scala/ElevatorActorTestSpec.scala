import java.time.Duration

import elevatorsystem.actor.Elevator
import model.Messages._
import model._
import model.TimerModel.{NextFloorReached, WaitingCompleted}

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers


class ElevatorActorTestSpec extends TestKit(ActorSystem("TestActorSystem")) with ImplicitSender
  with AnyWordSpecLike with Matchers with BeforeAndAfterAll with ReceivedRequestsProperties {

  val config = ElevatorConfig(ElevatorId(1), 7, Duration.ofSeconds(4), Duration.ofSeconds(6), true)
  val elevator = system.actorOf(Props(new Elevator(config)))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "The elevator Actor" must {

    "be correctly initialized" in {
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          NoRequest(Floor(0))
        )
      )
    }

    "Transit to Moving state after receiving first Request" in {
      elevator ! CallRequest(Floor(1), Down)
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Moving(Up, Floor(0), Some(Floor(1)), None,
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )
    }

    "Ignore floors with requests that have a different direction (Down) as long as a further request is present" in {
      elevator ! CallRequest(Floor(6), Down)
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Moving(Up, Floor(1), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )
    }

    "Answer requests that have the same direction (Up)" in {
      elevator ! CallRequest(Floor(2), Up)
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(2), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )
    }

    "Store requests that have been made with different directions (Up, Down) in the same floor and answer them" in {
      elevator ! CallRequest(Floor(3), Down)
      elevator ! CallRequest(Floor(3), Up)
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(2), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(3) -> ReceivedRequestDirection(false, true, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(3), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )

    }

    "Answer floor requests" in {
      elevator ! LandRequest(ElevatorId(1), Floor(4))
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(4), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )

    }

    "Respond to requests where a pickup and floor request have been made in the same floor" in {
      elevator ! LandRequest(ElevatorId(1), Floor(5))
      elevator ! CallRequest(Floor(5), Down)
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(5), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
              Floor(6) -> ReceivedRequestDirection(false, false, true)
            )
          )
        )
      )

    }

    "Answer furthest call even if it has an opposite direction" in {
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Up, Floor(6), Some(Floor(6)), Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
            )
          )
        )
      )

    }

    "Switch directions after responding to the furthest upward call" in {
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Moving(Down, Floor(6), None, Some(Floor(1)),
            Map(
              Floor(1) -> ReceivedRequestDirection(false, false, true),
            )
          )
        )
      )
    }

    "Respond to Requests that have the same direction (Down)" in {
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          Waiting(Down, Floor(1), None, Some(Floor(1)),
            Map.empty
          )
        )
      )

    }

    "Become Idle again since all requests have been answered" in {
      elevator ! ManualStep
      elevator ! GetStatus
      expectMsg(
        ElevatorStatus(
          ElevatorId(1),
          NoRequest(Floor(1))
        )
      )

    }

  }

}
