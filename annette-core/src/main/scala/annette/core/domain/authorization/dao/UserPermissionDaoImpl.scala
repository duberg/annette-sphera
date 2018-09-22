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
import javax.inject.Inject

import akka.Done
import annette.core.domain.authorization.model.{ UserPermission, _ }
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 01.03.17.
 */
class UserPermissionDaoImpl @Inject() (
  db: AuthorizationDb) extends UserPermissionDao {
  override def store(userPermission: UserPermission)(implicit ec: ExecutionContext): Future[Done] = {
    val userPermissionFuture = db.userPermissions.store(userPermission)
    val rolePermissionFuture = db.userPermissions.store(userPermission)
    for {
      _ <- userPermissionFuture
      _ <- rolePermissionFuture
    } yield Done
  }

  override def delete(tenantId: Tenant.Id, userId: User.Id, permissionId: Permission.Id,
    roleId: Role.Id)(implicit ec: ExecutionContext): Future[Done] = {
    val userPermissionFuture = db.userPermissions.deleteById(tenantId, userId, permissionId, roleId)
    val rolePermissionFuture = db.userPermissions.deleteById(tenantId, userId, permissionId, roleId)
    for {
      _ <- userPermissionFuture
      _ <- rolePermissionFuture
    } yield Done
  }

  override def getById(tenantId: Tenant.Id, userId: User.Id, permissionId: Permission.Id,
    roleId: Role.Id)(implicit ec: ExecutionContext): Future[Option[UserPermission]] = {
    db.userPermissions.getById(tenantId, userId, permissionId, roleId)
  }

  override def selectByTenantAndUserId(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]] = {
    db.userPermissions.selectByTenantAndUserId(tenantId, userId)
  }

  override def selectByTenantAndRoleId(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]] = {
    db.rolePermissions.selectByTenantAndRoleId(tenantId, roleId)
  }

  override def selectByTenantId(tenantId: Tenant.Id)(implicit ec: ExecutionContext): Future[List[UserPermission]] = {
    db.userPermissions.selectByTenantId(tenantId)
  }

  override def selectAll(implicit ec: ExecutionContext): Future[List[UserPermission]] = {
    db.userPermissions.selectAll
  }

}
