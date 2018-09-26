package annette.core.domain.tenancy.dao

import javax.inject._

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model._
import org.joda.time.DateTime

import scala.concurrent.{ ExecutionContext, Future }
import akka.Done
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.{ LogSource, Logging }
import akka.pattern.ask
import annette.core.domain.tenancy._
import annette.core.domain.tenancy.model.OpenSession.Id

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class SessionDao @Inject() (
  @Named("CoreService") actor: ActorRef, system: ActorSystem) extends ISessionDao {

  implicit val myLogSourceType: LogSource[SessionDao] = (a: SessionDao) => "SessionDao"

  val log = Logging(system, this)

  override def createSession(openSession: OpenSession)(implicit ec: ExecutionContext): Future[OpenSession] = {
    for {
      f <- ask(actor, OpenSessionService.CreateOpenSessionCmd(openSession))
    } yield {
      f match {
        case Done => openSession
        case OpenSessionService.EntryAlreadyExists => throw new SessionAlreadyExists
      }
    }
  }

  override def closeSession(id: OpenSession.Id)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      s <- ask(actor, OpenSessionService.FindOpenSessionById(id))
        .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry)
      session <- s.map(Future.successful).getOrElse(throw new SessionNotFound)
      sessionHistory <- Future.successful(SessionHistory(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id))
      lastSession <- Future.successful(LastSession(session.userId, session.tenantId, session.applicationId,
        session.languageId, session.startTimestamp, DateTime.now(), session.ip, session.id))
      _ <- ask(actor, OpenSessionService.DeleteOpenSessionCmd(id))
      _ <- ask(actor, LastSessionService.StoreLastSessionCmd(lastSession))
      _ <- ask(actor, SessionHistoryService.CreateSessionHistoryCmd(sessionHistory))
    } yield {}
  }

  override def updateLastOpTimestamp(id: OpenSession.Id)(implicit ec: ExecutionContext): Unit = {
    ask(actor, OpenSessionService.UpdateOpenSessionCmd(OpenSessionUpdate(
      id = id,
      lastOpTimestamp = Some(DateTime.now())))).failed.foreach(e => log.error(e.getMessage))

  }

  override def updateTenantApplicationLanguage(
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)(implicit ec: ExecutionContext): Unit = {
    ask(actor, OpenSessionService.UpdateTenantApplicationLanguageCmd(id, tenantId, applicationId, languageId))
      .failed.foreach(e => log.error(e.getMessage))
  }

  override def getOpenSessionById(id: OpenSession.Id)(implicit ec: ExecutionContext): Future[Option[OpenSession]] = {

    ask(actor, OpenSessionService.FindOpenSessionById(id))
      .mapTo[OpenSessionService.OpenSessionOpt].map(_.maybeEntry)

  }

  override def getLastSessionByUserId(userId: User.Id)(implicit ec: ExecutionContext): Future[Option[LastSession]] = {
    ask(actor, LastSessionService.FindLastSessionByUserId(userId)).mapTo[LastSessionService.LastSessionOpt].map(_.maybeEntry)
  }

  override def getSessionHistoryById(id: Id)(implicit ec: ExecutionContext): Future[Option[SessionHistory]] = {
    ask(actor, SessionHistoryService.FindSessionHistoryById(id))
      .mapTo[SessionHistoryService.SessionHistoryOpt].map(_.maybeEntry)
  }

  override def getAllOpenSessions(implicit ec: ExecutionContext): Future[Seq[OpenSession]] = {
    ask(actor, OpenSessionService.FindAllOpenSessions).mapTo[OpenSessionService.OpenSessionSeq].map(_.entries)
  }

  override def getAllLastSessions(implicit ec: ExecutionContext): Future[Seq[LastSession]] = {
    ask(actor, LastSessionService.FindAllLastSessions).mapTo[LastSessionService.LastSessionSeq].map(_.entries)
  }

  override def getAllSessionHistories(implicit ec: ExecutionContext): Future[Seq[SessionHistory]] = {
    ask(actor, SessionHistoryService.FindAllSessionHistory).mapTo[SessionHistoryService.SessionHistorySeq].map(_.entries)
  }
}
