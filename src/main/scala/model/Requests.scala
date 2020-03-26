package model

import Messages.{CallRequest, FloorRequest, LandRequest}

import cats.Monoid
import cats.syntax.semigroup._

/**
  * For each floor, requests of different direction as well as call and landing requests can be made at once.
  * This case class and its monoid instance helps represent them.
  */

case class ReceivedRequestDirection(floorRequest: Boolean, up: Boolean, down: Boolean)

trait ReceivedRequestsProperties {

  /**
    * This monoid helps with combining the information of different requests that have been made in the same floor.
    */

  implicit val ReceivedRequestDirectionMonoid: Monoid[ReceivedRequestDirection] = new Monoid[ReceivedRequestDirection] {
    def combine(a: ReceivedRequestDirection, b: ReceivedRequestDirection): ReceivedRequestDirection =
      ReceivedRequestDirection(a.floorRequest || b.floorRequest, a.up || b.up, a.down || b.down)
    def empty = ReceivedRequestDirection(false, false, false)
  }

  /**
    * Add a new request to the map of requests.
    */

  def addReceivedRequest(request :FloorRequest, destinations: Map[Floor, ReceivedRequestDirection] = Map.empty): Map[Floor, ReceivedRequestDirection] = {
    val oldRequestDirection = destinations.getOrElse(request.floor, Monoid[ReceivedRequestDirection].empty)
    request match {
      case r: LandRequest =>
        destinations + (r.floor -> (ReceivedRequestDirection(true, false, false) |+| oldRequestDirection))
      case r: CallRequest =>
        if (r.direction == Up) destinations + (r.floor -> (ReceivedRequestDirection(false, true, false) |+| oldRequestDirection))
        else destinations + (r.floor -> (ReceivedRequestDirection(false, false, true) |+| oldRequestDirection))
    }
  }

  /**
    * Remove already existing request.
    */

  def removeReceivedRequest(floor: Floor, destinations: Map[Floor, ReceivedRequestDirection]): Map[Floor, ReceivedRequestDirection] =
    destinations - floor

}
