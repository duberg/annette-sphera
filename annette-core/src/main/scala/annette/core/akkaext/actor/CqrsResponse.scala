package annette.core.akkaext.actor

import java.util.UUID

import akka.actor.ActorRef

trait CqrsResponse

object CqrsResponse {
  case object Pong extends CqrsResponse
  case object Terminated extends CqrsResponse
  case object Success extends CqrsResponse
  case object Persisted extends CqrsResponse
  case object NotPersisted extends CqrsResponse
  case class ChildOpt(actorRef: Option[ActorRef])
}
