package annette.core.domain.tenancy

import akka.actor.Props
import annette.core.akkaext.actor.{ ActorId, CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.domain.tenancy.actor.{ SessionHistoryActor, SessionHistoryState }
import annette.core.domain.tenancy.model._

object SessionHistoryService {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case object EntryAlreadyExists extends Response
  case object EntryNotFound extends Response

  case class CreateSessionHistoryCmd(entry: SessionHistory) extends Command
  case class FindSessionHistoryById(id: OpenSession.Id) extends Query
  case object FindAllSessionHistory extends Query

  case class SessionHistoryCreatedEvt(entry: SessionHistory) extends Event

  case class SessionHistoryOpt(maybeEntry: Option[SessionHistory]) extends Response
  case class SessionHistorySeq(entries: Seq[SessionHistory]) extends Response

  def props(id: ActorId, state: SessionHistoryState = SessionHistoryState()) =
    Props(new SessionHistoryActor(id, state))
}
