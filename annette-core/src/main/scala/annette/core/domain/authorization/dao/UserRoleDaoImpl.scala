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

import javax.inject._

import akka.Done
import annette.core.domain.authorization.model.{ Role, UserRole }
import annette.core.domain.authorization.{ RoleNotFound, UserRoleNotFound }
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class UserRoleDaoImpl @Inject() (
  db: AuthorizationDb) extends UserRoleDao {

  override def create(userRole: UserRole)(implicit ec: ExecutionContext): Future[Done] = {
    for {
      role <- validateCreate(userRole)
      createdUserRole <- createInternal(userRole, role)
    } yield Done
  }

  override def delete(tenantId: Tenant.Id, roleId: String, userId: User.Id)(implicit ec: ExecutionContext): Future[Done] = {
    for {
      userRole <- validateDelete(tenantId, roleId, userId)
      deletedUserRole <- deleteInternal(userRole)
    } yield Done
  }

  override def getById(tenantId: Tenant.Id, roleId: String, userId: User.Id)(implicit ec: ExecutionContext) =
    db.userRoles.getById(tenantId, roleId, userId)

  override def selectByRole(tenantId: Tenant.Id, roleId: Role.Id)(implicit ec: ExecutionContext): Future[List[UserRole]] =
    db.userRoles.selectByRole(tenantId, roleId)

  override def selectByTenant(tenantId: Tenant.Id)(implicit ec: ExecutionContext): Future[List[UserRole]] =
    db.userRoles.selectByTenant(tenantId)

  override def selectAll(implicit ec: ExecutionContext) = db.userRoles.selectAll

  private def validateCreate(userRole: UserRole)(implicit ec: ExecutionContext): Future[Role] = {
    for {
      roleOpt <- db.roles.getById(userRole.tenantId, userRole.roleId)
    } yield {
      roleOpt.getOrElse(throw new RoleNotFound())
    }
  }

  private def createInternal(userRole: UserRole, role: Role)(implicit ec: ExecutionContext): Future[UserRole] = {
    for {
      userRoleRes <- db.userRoles.store(userRole)
    } yield userRole
  }

  def validateDelete(tenantId: String, roleId: String, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      userRoleOpt <- db.userRoles.getById(tenantId, roleId, userId)
    } yield {
      val userRole = userRoleOpt.getOrElse(throw new UserRoleNotFound())
      userRole
    }
  }

  def deleteInternal(userRole: UserRole) = {
    db.userRoles.deleteById(userRole.tenantId, userRole.roleId, userRole.userId)
  }

}
