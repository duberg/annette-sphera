package annette.core.domain.tenancy.dao

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model._

trait ISessionDao {
  import akka.util.Timeout
  import scala.concurrent.duration._
  import scala.concurrent.{ ExecutionContext, Future }

  implicit val serviceTimeout: Timeout = 30.seconds

  def createSession(openSession: OpenSession)(implicit ec: ExecutionContext): Future[OpenSession]
  def closeSession(id: OpenSession.Id)(implicit ec: ExecutionContext): Future[Unit]
  def updateTenantApplicationLanguage(
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)(implicit ec: ExecutionContext): Unit

  def updateLastOpTimestamp(id: OpenSession.Id)(implicit ec: ExecutionContext): Unit

  def getOpenSessionById(id: OpenSession.Id)(implicit ec: ExecutionContext): Future[Option[OpenSession]]

  def getLastSessionByUserId(userId: User.Id)(implicit ec: ExecutionContext): Future[Option[LastSession]]

  def getSessionHistoryById(id: OpenSession.Id)(implicit ec: ExecutionContext): Future[Option[SessionHistory]]

  def getAllOpenSessions(implicit ec: ExecutionContext): Future[Seq[OpenSession]]

  def getAllLastSessions(implicit ec: ExecutionContext): Future[Seq[LastSession]]

  def getAllSessionHistories(implicit ec: ExecutionContext): Future[Seq[SessionHistory]]

}
