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
import annette.core.domain.authorization.model.Permission
import annette.core.domain.authorization.{ PermissionAlreadyExists, PermissionNotFound }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class PermissionDaoImpl @Inject() (
  db: AuthorizationDb) extends PermissionDao {

  override def create(permission: Permission)(implicit ec: ExecutionContext): Future[Permission] = {
    for {
      validatedPermission <- validateCreate(permission)
      createdPermission <- createInternal(permission)
    } yield createdPermission
  }

  override def update(permission: Permission)(implicit ec: ExecutionContext): Future[Permission] = {
    for {
      (oldPermission, validatedPermission) <- validateUpdate(permission)
      createdPermission <- updateInternal(oldPermission, validatedPermission)
    } yield createdPermission
  }

  override def delete(id: String)(implicit ec: ExecutionContext) = db.permissions.deleteById(id).map(r => Done)

  override def getById(id: String)(implicit ec: ExecutionContext) = db.permissions.getById(id)

  override def selectAll(implicit ec: ExecutionContext) = db.permissions.selectAll

  private def validateCreate(permission: Permission)(implicit ec: ExecutionContext): Future[Permission] = {
    for {
      permissionExist <- db.permissions.isExist(permission.id)
    } yield {
      if (permissionExist) throw new PermissionAlreadyExists()
      permission
    }
  }

  private def createInternal(permission: Permission)(implicit ec: ExecutionContext): Future[Permission] = {
    for {
      permissionRes <- db.permissions.store(permission)
    } yield permission
  }

  private def validateUpdate(permission: Permission)(implicit ec: ExecutionContext) = {
    for {
      permissionOpt <- db.permissions.getById(permission.id)
    } yield {
      // проверка существования пользователя
      val oldPermission = permissionOpt.getOrElse(throw new PermissionNotFound())
      (oldPermission, permission)
    }
  }

  private def updateInternal(oldPermission: Permission, newPermission: Permission)(implicit ec: ExecutionContext) = {
    for {
      permissionRes <- db.permissions.store(newPermission)
    } yield newPermission
  }

}
