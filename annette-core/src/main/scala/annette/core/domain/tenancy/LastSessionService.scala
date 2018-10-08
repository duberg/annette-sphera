package annette.core.domain.tenancy

import akka.actor.Props
import annette.core.akkaext.actor.{ ActorId, CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.domain.tenancy.actor.{ LastSessionActor, LastSessionState }
import annette.core.domain.tenancy.model._

object LastSessionService {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case object EntryAlreadyExists extends Response
  case object EntryNotFound extends Response

  case class StoreLastSessionCmd(entry: LastSession) extends Command
  case class FindLastSessionByUserId(id: User.Id) extends Query
  case object FindAllLastSessions extends Query

  case class LastSessionStoredEvt(entry: LastSession) extends Event

  case class LastSessionOpt(maybeEntry: Option[LastSession]) extends Response
  case class LastSessionSeq(entries: Seq[LastSession]) extends Response

  def props(id: ActorId, state: LastSessionState = LastSessionState()) =
    Props(new LastSessionActor(id, state))
}
