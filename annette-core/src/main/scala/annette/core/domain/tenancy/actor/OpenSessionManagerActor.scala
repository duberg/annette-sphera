package annette.core.domain.tenancy.actor

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ LastSessionManager, OpenSessionManager, SessionHistoryManager }
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class OpenSessionManagerActor(
  val id: ActorId,
  val lastSessionRef: ActorRef,
  val sessionHistoryRef: ActorRef,
  val initState: OpenSessionManagerState) extends CqrsPersistentActor[OpenSessionManagerState] {

  implicit val serviceTimeout: Timeout = 5.seconds
  implicit val ec: ExecutionContext = context.dispatcher

  def createOpenSession(state: OpenSessionManagerState, entry: OpenSession): Unit = {
    if (state.openSessionExists(entry.id)) sender ! OpenSessionManager.EntryAlreadyExists
    else {
      persist(OpenSessionManager.OpenSessionCreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def updateOpenSession(state: OpenSessionManagerState, entry: OpenSessionUpdate): Unit = {
    if (state.openSessionExists(entry.id)) {
      persist(OpenSessionManager.OpenSessionUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionManager.EntryNotFound
    }
  }

  def updateTenantApplicationLanguage(
    state: OpenSessionManagerState,
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
      persist(OpenSessionManager.OpenSessionUpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionManager.EntryNotFound
    }
  }

  private def isSessionExpired(session: OpenSession) = {
    if (session.rememberMe) {
      session.timeout != 0 && session.startTimestamp.plusMinutes(session.timeout).isBeforeNow
    } else {
      session.timeout != 0 && session.lastOpTimestamp.plusMinutes(session.timeout).isBeforeNow
    }
  }

  def deleteOpenSession(state: OpenSessionManagerState, id: OpenSession.Id): Unit = {
    if (state.openSessionExists(id)) {
      persist(OpenSessionManager.OpenSessionDeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else {
      sender ! OpenSessionManager.EntryNotFound
    }
  }

  def getOpenSessionById(state: OpenSessionManagerState, id: OpenSession.Id): Unit = {
    val openSessionOpt = state.findOpenSessionById(id)
    if (openSessionOpt.isDefined && isSessionExpired(openSessionOpt.get)) {
      val session = openSessionOpt.get
      val sessionHistory = SessionHistory(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id)
      val lastSession = LastSession(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id)
      val f = for {
        _ <- ask(self, OpenSessionManager.DeleteOpenSessionCmd(id))
        _ <- ask(lastSessionRef, LastSessionManager.StoreLastSessionCmd(lastSession))
        _ <- ask(sessionHistoryRef, SessionHistoryManager.CreateSessionHistoryCmd(sessionHistory))
      } yield {

      }
      f.failed.foreach(x => log.error(x.getMessage))

      sender ! OpenSessionManager.OpenSessionOpt(None)
    } else {
      sender ! OpenSessionManager.OpenSessionOpt(openSessionOpt)
    }

  }

  def findAllOpenSessions(state: OpenSessionManagerState): Unit =
    sender ! OpenSessionManager.OpenSessionSeq(state.findAllOpenSessions)

  def behavior(state: OpenSessionManagerState): Receive = {
    case OpenSessionManager.CreateOpenSessionCmd(entry) => createOpenSession(state, entry)
    case OpenSessionManager.UpdateOpenSessionCmd(entry) => updateOpenSession(state, entry)
    case OpenSessionManager.UpdateTenantApplicationLanguageCmd(x, y, z, u) =>
      updateTenantApplicationLanguage(state, x, y, z, u)
    case OpenSessionManager.DeleteOpenSessionCmd(i) => deleteOpenSession(state, i)
    case OpenSessionManager.FindOpenSessionById(i) => getOpenSessionById(state, i)
    case OpenSessionManager.FindAllOpenSessions => findAllOpenSessions(state)

  }

}
