package annette.core.domain.tenancy

import akka.actor.{ ActorRef, Props }
import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.actor.{ OpenSessionManagerActor, OpenSessionManagerState }
import annette.core.domain.tenancy.model._

object OpenSessionManager {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case object EntryAlreadyExists extends Response
  case object EntryNotFound extends Response

  case class CreateOpenSessionCmd(entry: OpenSession) extends Command
  case class UpdateOpenSessionCmd(entry: OpenSessionUpdate) extends Command
  case class UpdateTenantApplicationLanguageCmd(
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id) extends Command
  case class DeleteOpenSessionCmd(id: OpenSession.Id) extends Command

  case class FindOpenSessionById(id: OpenSession.Id) extends Query
  case object FindAllOpenSessions extends Query

  case class OpenSessionCreatedEvt(entry: OpenSession) extends Event
  case class OpenSessionUpdatedEvt(entry: OpenSessionUpdate) extends Event
  case class OpenSessionDeletedEvt(id: OpenSession.Id) extends Event

  case class OpenSessionOpt(maybeEntry: Option[OpenSession]) extends Response
  case class OpenSessionSeq(entries: Seq[OpenSession]) extends Response

  def props(lastSession: ActorRef, sessionHistory: ActorRef) =
    Props(new OpenSessionManagerActor(lastSession, sessionHistory))
}
