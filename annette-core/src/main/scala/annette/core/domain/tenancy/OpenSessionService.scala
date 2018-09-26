package annette.core.domain.tenancy

import akka.actor.{ ActorRef, Props }
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.actor.{ OpenSessionActor, OpenSessionState }
import annette.core.domain.tenancy.model._
import annette.core.persistence.Persistence.{ PersistentCommand, PersistentEvent, PersistentQuery }
import io.circe.Decoder.state

object OpenSessionService {

  sealed trait Command extends PersistentCommand
  sealed trait Query extends PersistentQuery
  sealed trait Event extends PersistentEvent
  sealed trait Response

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

  def props(id: String, lastSession: ActorRef, sessionHistory: ActorRef, state: OpenSessionState = OpenSessionState()) =
    Props(classOf[OpenSessionActor], id, lastSession, sessionHistory, state)
}
