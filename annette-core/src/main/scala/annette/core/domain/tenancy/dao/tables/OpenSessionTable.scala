package annette.core.domain.tenancy.dao.tables

import annette.core.domain.application.model.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ OpenSession, Tenant }
import com.outworkers.phantom
import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Created by valery on 16.12.16.
 */

private[dao] abstract class OpenSessionTable extends Table[OpenSessionTable, OpenSession] with RootConnector {
  object userId extends UUIDColumn
  object tenantId extends StringColumn
  object applicationId extends StringColumn
  object languageId extends StringColumn
  object startTimestamp extends DateTimeColumn
  object lastOpTimestamp extends DateTimeColumn
  object rememberMe extends BooleanColumn
  object timeout extends IntColumn
  object ip extends StringColumn
  object timestamp extends DateTimeColumn
  object id extends UUIDColumn with PartitionKey

  override def fromRow(r: Row): OpenSession = {
    OpenSession(userId(r), tenantId(r), applicationId(r), languageId(r),
      startTimestamp(r), lastOpTimestamp(r), rememberMe(r), timeout(r), ip(r), timestamp(r), id(r))
  }

  override def tableName: String = "core_open_sessions"

  def store(openSession: OpenSession): Future[ResultSet] = {
    insert.value(_.id, openSession.id)
      .value(_.userId, openSession.userId)
      .value(_.tenantId, openSession.tenantId)
      .value(_.applicationId, openSession.applicationId)
      .value(_.languageId, openSession.languageId)
      .value(_.startTimestamp, openSession.startTimestamp)
      .value(_.lastOpTimestamp, openSession.lastOpTimestamp)
      .value(_.rememberMe, openSession.rememberMe)
      .value(_.timeout, openSession.timeout)
      .value(_.ip, openSession.ip)
      .value(_.timestamp, openSession.timestamp)
      .future()
  }

  def updateLastOpTimestamp(id: OpenSession.Id): Future[phantom.ResultSet] = {
    update
      .where(_.id eqs id)
      .modify(_.lastOpTimestamp setTo DateTime.now())
      .future()
  }

  def updateTenantApplicationLanguage(
    id: OpenSession.Id,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id): Future[phantom.ResultSet] = {
    update
      .where(_.id eqs id)
      .modify(_.tenantId setTo tenantId)
      .and(_.applicationId setTo applicationId)
      .and(_.languageId setTo languageId)
      .and(_.lastOpTimestamp setTo DateTime.now())
      .future()
  }

  def getById(id: OpenSession.Id): Future[Option[OpenSession]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: OpenSession.Id): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def isExist(id: OpenSession.Id): Future[Boolean] = {
    select(_.id).where(_.id eqs id).one().map(_.isDefined)
  }
  def selectAll: Future[List[OpenSession]] = {
    select.fetch
  }

}

