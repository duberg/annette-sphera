package annette.core.domain.authorization.service

import akka.Done
import annette.core.domain.authorization.model.Role
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 01.03.17.
 */
trait RoleActivation {
  def activate(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[Done]
  def updateRolePermissionsForUser(
    tenantId: Tenant.Id,
    roleId: Role.Id, userId: User.Id)(implicit ec: ExecutionContext): Future[Done]

}
