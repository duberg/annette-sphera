package annette.core.domain.tenancy.dao.tables

import annette.core.domain.tenancy.model.{ Tenant, TenantUser, User }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class TenantUserTable extends Table[TenantUserTable, TenantUser] with RootConnector {

  object tenantId extends StringColumn with PartitionKey
  object userId extends UUIDColumn with PrimaryKey

  override def fromRow(row: Row): TenantUser = {
    TenantUser(tenantId(row), userId(row))
  }

  override def tableName: Tenant.Id = "core_tenant_users"

  def store(entity: TenantUser): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.userId, entity.userId)
      .future()
  }

  def store(tenantId: Tenant.Id, userId: User.Id): Future[ResultSet] = {
    insert
      .value(_.tenantId, tenantId)
      .value(_.userId, userId)
      .future()
  }

  def getById(tenantId: Tenant.Id, userId: User.Id): Future[Option[TenantUser]] = {
    select.where(_.tenantId eqs tenantId).and(_.userId eqs userId).one()
  }

  def getByTenantId(tenantId: Tenant.Id) = {
    select.where(_.tenantId eqs tenantId).fetch()
  }

  def getByUserId(userId: User.Id) = {
    select.where(_.userId eqs userId).allowFiltering().fetch()
  }

  def deleteById(tenantId: Tenant.Id, userId: User.Id): Future[ResultSet] = {
    delete.where(_.tenantId eqs tenantId).and(_.userId eqs userId).future()
  }

  def isExist(tenantId: Tenant.Id, userId: User.Id): Future[Boolean] = {
    getById(tenantId, userId).map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
