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
