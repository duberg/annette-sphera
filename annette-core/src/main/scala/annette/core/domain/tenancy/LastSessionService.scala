package annette.core.domain.tenancy

import akka.actor.Props
import annette.core.domain.tenancy.actor.{LastSessionActor, LastSessionState}
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence.{PersistentCommand, PersistentEvent, PersistentQuery}

object LastSessionService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

  case object EntryAlreadyExists extends Response
  case object EntryNotFound extends Response

  case class StoreLastSessionCmd(entry: LastSession) extends Command
  case class FindLastSessionByUserId(id: User.Id) extends Query
  case object FindAllLastSessions extends Query

  case class LastSessionStoredEvt(entry: LastSession) extends Event

  case class LastSessionOpt(maybeEntry: Option[LastSession]) extends Response
  case class LastSessionSeq(entries: Seq[LastSession]) extends Response

  def props(id: String, state: LastSessionState = LastSessionState()) = Props(classOf[LastSessionActor], id, state)
}
