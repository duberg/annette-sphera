package annette.core.domain.tenancy.dao.tables

import annette.core.domain.tenancy.model.{ Tenant, TenantUserRole, User }
import com.outworkers.phantom.dsl.{ PrimaryKey, _ }

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class TenantUserRoleTable extends Table[TenantUserRoleTable, TenantUserRole] with RootConnector {

  object tenantId extends StringColumn with PartitionKey
  object userId extends UUIDColumn with PrimaryKey
  object roles extends SetColumn[String]

  override def fromRow(row: Row): TenantUserRole = {
    TenantUserRole(tenantId(row), userId(row), roles(row))
  }

  override def tableName: Tenant.Id = "core_tenant_user_roles"

  def store(entity: TenantUserRole): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.userId, entity.userId)
      .value(_.roles, entity.roles)
      .future()
  }

  def getById(tenantId: Tenant.Id, userId: User.Id): Future[Option[TenantUserRole]] = {
    select.where(_.tenantId eqs tenantId).and(_.userId eqs userId).one()
  }

  def getByTenantId(tenantId: Tenant.Id) = {
    select.where(_.tenantId eqs tenantId).fetch()
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
