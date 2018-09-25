package annette.core.domain.authorization.dao.tables

import annette.core.domain.authorization.model.UserRole
import annette.core.domain.tenancy.model.{ Tenant, User }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class UserRoleTable extends Table[UserRoleTable, UserRole] with RootConnector {

  object tenantId extends StringColumn with PartitionKey
  object roleId extends StringColumn with PrimaryKey
  object userId extends UUIDColumn with PrimaryKey

  override def fromRow(row: Row): UserRole = {
    UserRole(tenantId(row), roleId(row), userId(row))
  }

  override def tableName: String = "core_user_roles"

  def store(entity: UserRole): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.roleId, entity.roleId)
      .value(_.userId, entity.userId)
      .future()
  }

  def getById(tenantId: Tenant.Id, roleId: String, userId: User.Id): Future[Option[UserRole]] = {
    select.where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).and(_.userId eqs userId).one()
  }

  def deleteById(tenantId: Tenant.Id, roleId: String, userId: User.Id): Future[ResultSet] = {
    delete.where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).and(_.userId eqs userId).future()
  }

  def isExist(tenantId: Tenant.Id, roleId: String, userId: User.Id): Future[Boolean] = {
    select(_.userId)
      .where(_.tenantId eqs tenantId).and(_.roleId eqs roleId).and(_.userId eqs userId)
      .one()
      .map(_.isDefined)
  }

  def selectByRole(tenantId: Tenant.Id, roleId: String) = {
    select
      .where(_.tenantId eqs tenantId).and(_.roleId eqs roleId)
      .fetch()
  }

  def selectByTenant(tenantId: Tenant.Id): Future[List[UserRole]] = {
    select.where(_.tenantId eqs tenantId).fetch()
  }

  def selectAll = {
    select.fetch
  }
}
