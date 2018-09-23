package annette.core.domain.tenancy.actor

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.application.model.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ LastSessionService, OpenSessionService, SessionHistoryService }
import annette.core.persistence.Persistence._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

class OpenSessionActor(val id: String, val lastSessionRef: ActorRef, val sessionHistoryRef: ActorRef,
  val initState: OpenSessionState) extends PersistentStateActor[OpenSessionState] {
  import scala.concurrent.duration._

  implicit val serviceTimeout: Timeout = 5.seconds
  implicit val ec: ExecutionContext = context.dispatcher

  def createOpenSession(state: OpenSessionState, entry: OpenSession): Unit = {
    if (state.openSessionExists(entry.id)) sender ! OpenSessionService.EntryAlreadyExists
    else {
      persist(OpenSessionService.OpenSessionCreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def updateOpenSession(state: OpenSessionState, entry: OpenSessionUpdate): Unit = {
    if (state.openSessionExists(entry.id)) {
      persist(OpenSessionService.OpenSessionUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionService.EntryNotFound
    }
  }

  def updateTenantApplicationLanguage(
    state: OpenSessionState,
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id): Unit = {
    val entry = OpenSessionUpdate(
      id = id,
      tenantId = Some(tenantId),
      applicationId = Some(applicationId),
      languageId = Some(languageId))
    if (state.openSessionExists(entry.id)) {
      persist(OpenSessionService.OpenSessionUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionService.EntryNotFound
    }
  }

  private def isSessionExpired(session: OpenSession) = {
    if (session.rememberMe) {
      session.timeout != 0 && session.startTimestamp.plusMinutes(session.timeout).isBeforeNow
    } else {
      session.timeout != 0 && session.lastOpTimestamp.plusMinutes(session.timeout).isBeforeNow
    }
  }

  def deleteOpenSession(state: OpenSessionState, id: OpenSession.Id): Unit = {
    if (state.openSessionExists(id)) {
      persist(OpenSessionService.OpenSessionDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionService.EntryNotFound
    }
  }

  def getOpenSessionById(state: OpenSessionState, id: OpenSession.Id): Unit = {
    val openSessionOpt = state.findOpenSessionById(id)
    if (openSessionOpt.isDefined && isSessionExpired(openSessionOpt.get)) {
      val session = openSessionOpt.get
      val sessionHistory = SessionHistory(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id)
      val lastSession = LastSession(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id)
      val f = for {
        _ <- ask(self, OpenSessionService.DeleteOpenSessionCmd(id))
        _ <- ask(lastSessionRef, LastSessionService.StoreLastSessionCmd(lastSession))
        _ <- ask(sessionHistoryRef, SessionHistoryService.CreateSessionHistoryCmd(sessionHistory))
      } yield {

      }
      f.failed.foreach(x => log.error(x.getMessage))

      sender ! OpenSessionService.OpenSessionOpt(None)
    } else {
      sender ! OpenSessionService.OpenSessionOpt(openSessionOpt)
    }

  }

  def findAllOpenSessions(state: OpenSessionState): Unit =
    sender ! OpenSessionService.OpenSessionSeq(state.findAllOpenSessions)

  def behavior(state: OpenSessionState): Receive = {
    case OpenSessionService.CreateOpenSessionCmd(entry) => createOpenSession(state, entry)
    case OpenSessionService.UpdateOpenSessionCmd(entry) => updateOpenSession(state, entry)
    case OpenSessionService.UpdateTenantApplicationLanguageCmd(x, y, z, u) =>
      updateTenantApplicationLanguage(state, x, y, z, u)
    case OpenSessionService.DeleteOpenSessionCmd(i) => deleteOpenSession(state, i)
    case OpenSessionService.FindOpenSessionById(i) => getOpenSessionById(state, i)
    case OpenSessionService.FindAllOpenSessions => findAllOpenSessions(state)

  }

}
