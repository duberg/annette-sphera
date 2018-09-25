package annette.core.domain.authorization.dao
import akka.Done
import annette.core.domain.authorization.model.Role
import annette.core.domain.tenancy.model.Tenant

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 01.03.17.
 */
trait RoleDao {
  def create(role: Role)(implicit ec: ExecutionContext): Future[Role]
  def update(role: Role)(implicit ec: ExecutionContext): Future[Role]
  def updateActivated(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[Done]
  def delete(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[Done]
  def getById(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[Option[Role]]
  def selectByTenantId(tenantId: Tenant.Id)(implicit ec: ExecutionContext): Future[List[Role]]
  def selectAll(implicit ec: ExecutionContext): Future[List[Role]]
}
