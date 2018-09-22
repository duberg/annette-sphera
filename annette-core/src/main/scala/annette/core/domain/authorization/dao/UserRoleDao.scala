/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.dao
import akka.Done
import annette.core.domain.authorization.model.{ Role, UserRole }
import annette.core.domain.tenancy.model._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 27.02.17.
 */
trait UserRoleDao {
  def create(userRole: UserRole)(implicit ec: ExecutionContext): Future[Done]
  def delete(tenantId: Tenant.Id, roleId: Role.Id, userId: User.Id)(implicit ec: ExecutionContext): Future[Done]
  def getById(tenantId: Tenant.Id, roleId: Role.Id, userId: User.Id)(implicit ec: ExecutionContext): Future[Option[UserRole]]
  def selectByRole(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[List[UserRole]]
  def selectByTenant(tenantId: Tenant.Id)(implicit ec: ExecutionContext): Future[List[UserRole]]
  def selectAll(implicit ec: ExecutionContext): Future[List[UserRole]]
}
