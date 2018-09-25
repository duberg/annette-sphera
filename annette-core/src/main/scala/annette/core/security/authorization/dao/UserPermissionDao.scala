package annette.core.domain.authorization.dao

import akka.Done
import annette.core.domain.authorization.model.{ Permission, Role, UserPermission }
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 01.03.17.
 */
trait UserPermissionDao {
  def store(userPermission: UserPermission)(implicit ec: ExecutionContext): Future[Done]

  def delete(tenantId: Tenant.Id, userId: User.Id, permissionId: Permission.Id,
    roleId: Role.Id)(implicit ec: ExecutionContext): Future[Done]
  def getById(tenantId: Tenant.Id, userId: User.Id, permissionId: Permission.Id,
    roleId: Role.Id)(implicit ec: ExecutionContext): Future[Option[UserPermission]]
  def selectByTenantAndUserId(
    tenantId: Tenant.Id,
    userId: User.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]]
  def selectByTenantAndRoleId(
    tenantId: Tenant.Id,
    roleId: Role.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]]
  def selectByTenantId(tenantId: Tenant.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]]
  def selectAll(implicit ec: ExecutionContext): Future[List[UserPermission]]

}
