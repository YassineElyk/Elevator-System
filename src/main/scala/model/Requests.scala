package model

import Messages.{CallRequest, ElevatorRequest, LandingRequest}

import cats.Monoid
import cats.syntax.semigroup._

case class ReceivedRequestDirection(floorRequest: Boolean, up: Boolean, down: Boolean)

trait ReceivedRequestsProperties {

  implicit val ReceivedRequestDirectionMonoid: Monoid[ReceivedRequestDirection] = new Monoid[ReceivedRequestDirection] {
    def combine(a: ReceivedRequestDirection, b: ReceivedRequestDirection): ReceivedRequestDirection =
      ReceivedRequestDirection(a.floorRequest || b.floorRequest, a.up || b.up, a.down || b.down)
    def empty = ReceivedRequestDirection(false, false, false)
  }

  def addReceivedRequest(request :ElevatorRequest, destinations: Map[Floor, ReceivedRequestDirection]): Map[Floor, ReceivedRequestDirection] = {
    val oldRequestDirection = destinations.getOrElse(request.floor, Monoid[ReceivedRequestDirection].empty)
    request match {
      case r: LandingRequest =>
        destinations + (r.floor -> (ReceivedRequestDirection(true, false, false) |+| oldRequestDirection))
      case r: CallRequest =>
        if (r.direction == Up()) destinations + (r.floor -> (ReceivedRequestDirection(false, true, false) |+| oldRequestDirection))
        else destinations + (r.floor -> (ReceivedRequestDirection(false, false, true) |+| oldRequestDirection))
    }
  }

  def removeReceivedRequest(floor: Floor, destinations: Map[Floor, ReceivedRequestDirection]): Map[Floor, ReceivedRequestDirection] =
    destinations + (floor -> Monoid[ReceivedRequestDirection].empty)

  def initReceivedRequests(floorCount: Int): Map[Floor, ReceivedRequestDirection] =
    (0 until floorCount).map(x => (Floor(x), Monoid[ReceivedRequestDirection].empty)).toMap


}
