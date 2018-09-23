/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.dao

import javax.inject._
import annette.core.domain.application.dao.ApplicationDao
import annette.core.domain.application.model.Application
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.{UserService, _}
import annette.core.domain.tenancy.model.{Tenant, TenantUserRole, User}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class TenantUserRoleDao @Inject() (
                                    db: TenancyDb,
                                    userDao: UserService,
                                  ) {

  private def validateStore(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      tenantExist <- db.tenants.isExist(tenantId)
      userExist <- userDao.getById(userId).map(_.nonEmpty)
    } yield {
      if (!tenantExist) throw new TenantNotFound()
      if (!userExist) throw new UserNotFound(userId)
      (tenantId, userId)
    }
  }

  private def storeInternal(roles: TenantUserRole)(implicit ec: ExecutionContext) = {
    for {
      tenantUserRoleRes <- db.tenantUserRoles.store(roles)
    } yield (roles)
  }

  def store(roles: TenantUserRole)(implicit ec: ExecutionContext) = {
    for {
      validatedTenantUserRole <- validateStore(roles.tenantId, roles.userId)
      createdTenantUserRole <- storeInternal(roles)
    } yield createdTenantUserRole
  }

  def delete(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      _ <- db.tenantUserRoles.deleteById(tenantId, userId)
    } yield ()
  }

  def getByIds(tenantId: Tenant.Id, userId: User.Id) = db.tenantUserRoles.getById(tenantId, userId)

  def selectAll = db.tenantUserRoles.selectAll

}

