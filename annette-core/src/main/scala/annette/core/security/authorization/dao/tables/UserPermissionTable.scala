package annette.core.domain.authorization.dao.tables

import annette.core.domain.authorization.model.UserPermission
import annette.core.domain.tenancy.model.{ Tenant, User }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class UserPermissionTable extends Table[UserPermissionTable, UserPermission] with RootConnector {

  object tenantId extends StringColumn with PartitionKey
  object userId extends UUIDColumn with PrimaryKey
  object permissionId extends StringColumn with PrimaryKey
  object roleId extends StringColumn with PrimaryKey
  object keys extends SetColumn[String]

  override def fromRow(row: Row): UserPermission = {
    UserPermission(tenantId(row), userId(row), permissionId(row), roleId(row), keys(row))
  }

  override def tableName: String = "core_user_permissions"

  def store(entity: UserPermission): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.userId, entity.userId)
      .value(_.permissionId, entity.permissionId)
      .value(_.roleId, entity.roleId)
      .future()
  }

  def deleteById(tenantId: Tenant.Id, userId: User.Id, permissionId: String, roleId: String): Future[ResultSet] = {
    delete
      .where(_.tenantId eqs tenantId)
      .and(_.userId eqs userId)
      .and(_.permissionId eqs permissionId)
      .and(_.roleId eqs roleId)
      .future()
  }

  def getById(tenantId: Tenant.Id, userId: User.Id, permissionId: String, roleId: String): Future[Option[UserPermission]] = {
    select
      .where(_.tenantId eqs tenantId)
      .and(_.userId eqs userId)
      .and(_.permissionId eqs permissionId)
      .and(_.roleId eqs roleId)
      .one()
  }
  def selectByTenantAndUserId(
    tenantId: Tenant.Id,
    userId: User.Id): Future[List[UserPermission]] = {
    select
      .where(_.tenantId eqs tenantId)
      .and(_.userId eqs userId)
      .fetch()
  }

  def selectByTenantId(tenantId: Tenant.Id): Future[List[UserPermission]] = {
    select
      .where(_.tenantId eqs tenantId)
      .fetch()
  }

  def isExist(tenantId: Tenant.Id, userId: User.Id, permissionId: String, roleId: String): Future[Boolean] = {
    select(_.userId)
      .where(_.tenantId eqs tenantId)
      .and(_.userId eqs userId)
      .and(_.permissionId eqs permissionId)
      .and(_.roleId eqs roleId)
      .one()
      .map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }

}
