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
import annette.core.domain.authorization.model.Role
import annette.core.domain.authorization.{ RoleAlreadyExists, RoleAssignedToUser, RoleNotFound }
import annette.core.domain.tenancy.model.Tenant.Id

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class RoleDaoImpl @Inject() (
  db: AuthorizationDb) extends RoleDao {

  override def create(role: Role)(implicit ec: ExecutionContext): Future[Role] = {
    val deactivatedRole = if (role.activated) role.copy(activated = false) else role
    for {
      validatedRole <- validateCreate(deactivatedRole)
      createdRole <- createInternal(deactivatedRole)
    } yield createdRole
  }

  override def update(role: Role)(implicit ec: ExecutionContext): Future[Role] = {
    val deactivatedRole = if (role.activated) role.copy(activated = false) else role
    for {
      (oldRole, validatedRole) <- validateUpdate(deactivatedRole)
      createdRole <- updateInternal(oldRole, validatedRole)
    } yield createdRole
  }

  override def delete(tenantId: String, roleId: String)(implicit ec: ExecutionContext) = {
    (for {
      role <- validateDelete(tenantId, roleId)
      deletedRole <- deleteInternal(role)
    } yield deletedRole).map(a => Done)
  }

  override def updateActivated(tenantId: Id, roleId: Id)(implicit ec: ExecutionContext): Future[Done] = {
    db.roles.updateActivated(tenantId, roleId).map(a => Done)
  }

  override def getById(tenantId: String, roleId: String)(implicit ec: ExecutionContext) =
    db.roles.getById(tenantId, roleId)

  override def selectByTenantId(tenantId: Id)(implicit ec: ExecutionContext) =
    db.roles.selectByTenantId(tenantId)

  override def selectAll(implicit ec: ExecutionContext) = db.roles.selectAll

  private def validateCreate(role: Role)(implicit ec: ExecutionContext): Future[Role] = {
    for {
      roleExist <- db.roles.isExist(role.tenantId, role.roleId)
    } yield {
      if (roleExist) throw new RoleAlreadyExists()
      role
    }
  }

  private def createInternal(role: Role)(implicit ec: ExecutionContext): Future[Role] = {
    for {
      roleRes <- db.roles.store(role)
    } yield role
  }

  private def validateUpdate(role: Role)(implicit ec: ExecutionContext) = {
    for {
      roleOpt <- db.roles.getById(role.tenantId, role.roleId)
    } yield {
      //  проверка существования роли
      val oldRole = roleOpt.getOrElse(throw new RoleNotFound())
      (oldRole, role)
    }
  }

  private def updateInternal(oldRole: Role, newRole: Role)(implicit ec: ExecutionContext) = {
    for {
      roleRes <- db.roles.store(newRole)
    } yield newRole
  }

  /* private def updateUserPermissions(oldRole: Role, newRole: Role)(implicit ec: ExecutionContext) = {
     val oldPerm = oldRole.permissions
     var newPerm = newRole.permissions.map(p => p)

     val res1 = Future.sequence(
       oldPerm.map { p =>
         if (newPerm.contains(p)) {
           newPerm -= p
           Future.successful(true)
         } else {
           db.userPermissions
             .deleteRolePermissionForAllUsers(oldRole.tenantId, p, oldRole.roleId)
             .map(a => true)
         }
       }
         .toList
     )

     val res4 = for {
       userRoles <- db.userRoles.selectByRole(oldRole.tenantId, oldRole.roleId)
       res2 = newPerm.toList.flatMap { p =>
         userRoles.map{ ur => db.userPermissions.store(
           UserPermission(oldRole.tenantId, ur.userId, p, oldRole.roleId)
         )}
       }
     } yield { res2.map(a => true) }

     for {
       r1 <- res1
       r2 <- res4
     } yield r1 ++: r2

   }*/

  def validateDelete(tenantId: String, roleId: String)(implicit ec: ExecutionContext) = {
    for {
      roleOpt <- db.roles.getById(tenantId, roleId)
      userRoles <- db.userRoles.selectByRole(tenantId, roleId)
    } yield {
      val role = roleOpt.getOrElse(throw new RoleNotFound())
      if (!userRoles.isEmpty) throw new RoleAssignedToUser()
      role
    }
  }

  def deleteInternal(role: Role) = {
    db.roles.deleteById(role.tenantId, role.roleId)
    // TOOD: удалить в таблице userPermissions
    // db.userPermissions.
  }

}
