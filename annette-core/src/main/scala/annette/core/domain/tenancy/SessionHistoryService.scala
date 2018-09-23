package annette.core.domain.tenancy

import akka.actor.Props
import annette.core.domain.tenancy.actor.{SessionHistoryActor, SessionHistoryState}
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence.{PersistentCommand, PersistentEvent, PersistentQuery}

object SessionHistoryService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case object EntryAlreadyExists extends Response
  case object EntryNotFound extends Response

  case class CreateSessionHistoryCmd(entry: SessionHistory) extends Command
  case class FindSessionHistoryById(id: OpenSession.Id) extends Query
  case object FindAllSessionHistory extends Query

  case class SessionHistoryCreatedEvt(entry: SessionHistory) extends Event

  case class SessionHistoryOpt(maybeEntry: Option[SessionHistory]) extends Response
  case class SessionHistorySeq(entries: Seq[SessionHistory]) extends Response

  def props(id: String, state: SessionHistoryState = SessionHistoryState()) = Props(classOf[SessionHistoryActor], id, state)
}
