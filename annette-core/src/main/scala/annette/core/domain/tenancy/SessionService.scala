package annette.core.domain.tenancy

import java.time.LocalDateTime

import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.{ LogSource, Logging }
import akka.pattern.ask
import akka.util.Timeout
import annette.core.akkaext.http.PageRequest
import annette.core.akkaext.http.Pagination.paginate
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.OpenSession.Id
import annette.core.domain.tenancy.model._
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SessionService @Inject() (@Named("CoreService") actor: ActorRef, system: ActorSystem)(implicit val c: ExecutionContext, val t: Timeout) {

  implicit val myLogSourceType: LogSource[SessionService] = (a: SessionService) => "SessionDao"

  val log = Logging(system, this)

  def createSession(openSession: OpenSession): Future[OpenSession] = {
    for {
      f <- ask(actor, OpenSessionManager.CreateOpenSessionCmd(openSession))
    } yield {
      f match {
        case Done => openSession
        case OpenSessionManager.EntryAlreadyExists => throw new SessionAlreadyExists
      }
    }
  }

  def closeSession(id: OpenSession.Id): Future[Unit] = {
    for {
      s <- ask(actor, OpenSessionManager.FindOpenSessionById(id))
        .mapTo[OpenSessionManager.OpenSessionOpt].map(_.maybeEntry)
      session <- s.map(Future.successful).getOrElse(throw new SessionNotFound)
      sessionHistory <- Future.successful(SessionHistory(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, LocalDateTime.now(), session.ip, session.id))
      lastSession <- Future.successful(LastSession(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, LocalDateTime.now(), session.ip, session.id))
      _ <- ask(actor, OpenSessionManager.DeleteOpenSessionCmd(id))
      _ <- ask(actor, LastSessionManager.StoreLastSessionCmd(lastSession))
      _ <- ask(actor, SessionHistoryManager.CreateSessionHistoryCmd(sessionHistory))
    } yield {}
  }

  def updateLastOpTimestamp(id: OpenSession.Id): Unit = {
    ask(actor, OpenSessionManager.UpdateOpenSessionCmd(OpenSessionUpdate(
      id = id,
      lastOpTimestamp = Some(LocalDateTime.now())))).failed.foreach(e => log.error(e.getMessage))

  }

  def updateTenantApplicationLanguage(
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id): Unit = {
    ask(actor, OpenSessionManager.UpdateTenantApplicationLanguageCmd(id, tenantId, applicationId, languageId))
      .failed.foreach(e => log.error(e.getMessage))
  }

  def getOpenSessionById(id: OpenSession.Id): Future[Option[OpenSession]] = {

    ask(actor, OpenSessionManager.FindOpenSessionById(id))
      .mapTo[OpenSessionManager.OpenSessionOpt].map(_.maybeEntry)

  }

  def getLastSessionByUserId(userId: User.Id): Future[Option[LastSession]] = {
    ask(actor, LastSessionManager.FindLastSessionByUserId(userId)).mapTo[LastSessionManager.LastSessionOpt].map(_.maybeEntry)
  }

  def getSessionHistoryById(id: Id): Future[Option[SessionHistory]] = {
    ask(actor, SessionHistoryManager.FindSessionHistoryById(id))
      .mapTo[SessionHistoryManager.SessionHistoryOpt].map(_.maybeEntry)
  }

  def listOpenSessions: Future[Seq[OpenSession]] = {
    ask(actor, OpenSessionManager.FindAllOpenSessions).mapTo[OpenSessionManager.OpenSessionSeq].map(_.entries)
  }

  def paginateOpenSessions(page: PageRequest): Future[PaginateOpenSessions] = {
    listOpenSessions.map { list =>
      val (items, totalCount) = paginate(list, page)
      PaginateOpenSessions(items, totalCount)
    }
  }

  def listLastSessions: Future[Seq[LastSession]] = {
    ask(actor, LastSessionManager.FindAllLastSessions).mapTo[LastSessionManager.LastSessionSeq].map(_.entries)
  }

  def listSessionHistories: Future[Seq[SessionHistory]] = {
    ask(actor, SessionHistoryManager.FindAllSessionHistory).mapTo[SessionHistoryManager.SessionHistorySeq].map(_.entries)
  }
}
