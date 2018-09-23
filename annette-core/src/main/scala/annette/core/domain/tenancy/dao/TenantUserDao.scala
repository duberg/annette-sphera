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
import annette.core.domain.tenancy.{ UserService, _ }
import annette.core.domain.tenancy.model.{ Tenant, User }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 18.12.16.
 */
@Singleton
class TenantUserDao @Inject() (
  db: TenancyDb,
  userDao: UserService,
  tenantDao: TenantDao,
  languageDao: LanguageDao,
  applicationDao: ApplicationDao) {

  private def validateCreate(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      tenantExist <- db.tenants.isExist(tenantId)
      userExist <- userDao.getById(userId).map(_.nonEmpty)
    } yield {
      // проверка существования пользователя
      if (!tenantExist) throw new TenantNotFound()
      if (!userExist) throw new UserNotFound(userId)
      (tenantId, userId)
    }
  }

  private def createInternal(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      tenantUserRes <- db.tenantUsers.store(tenantId, userId)
      userTenantRes <- db.userTenants.store(tenantId, userId)

    } yield (tenantId, userId)
  }

  def create(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      validatedTenantUser <- validateCreate(tenantId, userId)
      createdTenantUser <- createInternal(tenantId, userId)
    } yield createdTenantUser
  }

  def delete(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
    for {
      _ <- db.tenantUsers.deleteById(tenantId, userId)
      _ <- db.userTenants.deleteById(tenantId, userId)
    } yield ()
  }

  def getByIds(tenantId: Tenant.Id, userId: User.Id) = db.tenantUsers.getById(tenantId, userId)

  def getTenantUsers(tenantId: Tenant.Id) = {
    db.tenantUsers.getByTenantId(tenantId)
  }

  def getUserTenants(userId: User.Id) = {

    db.userTenants.getByUserId(userId)
  }

  def isExist(tenantId: Tenant.Id, userId: User.Id) = db.tenantUsers.isExist(tenantId, userId)

  def selectAll = db.tenantUsers.selectAll

  def getUserTenantData(userId: User.Id)(implicit ec: ExecutionContext): Future[Seq[TenantData]] = {
    val tenantsFuture =
      getUserTenants(userId).flatMap {
        userTenantIds =>
          Future.sequence(userTenantIds.map(_.tenantId).map(t => tenantDao.getById(t))).map(_.flatten)
      }
    val applicationsFuture = tenantsFuture.flatMap {
      tenants =>
        val applicationIds = tenants.flatMap(_.applications)
        Future.sequence(applicationIds.map(a => applicationDao.getById(a))).map(_.flatten)
    }
    val languagesFuture = tenantsFuture.flatMap {
      tenants =>
        val languageIds = tenants.flatMap(_.languages)
        Future.sequence(languageIds.map(a => languageDao.getById(a))).map(_.flatten)
    }

    for {
      tenants <- tenantsFuture.map(_.toSeq)
      applications <- applicationsFuture.map(_.toSeq)
      languages <- languagesFuture.map(_.toSeq)
    } yield {
      println("getUserTenantData")
      println(tenants)
      println(applications)
      println(languages)
      val appMap = applications.map(a => a.id -> a).toMap
      val langMap = languages.map(a => a.id -> a).toMap
      tenants.map {
        tenant =>
          val apps = tenant.applications
            .map(a => appMap.get(a))
            .flatten.toSeq
          val langs = tenant.languages
            .map(a => langMap.get(a))
            .flatten.toSeq
          val lang = langMap.get(tenant.languageId).get
          TenantData(tenant.name, apps, lang, langs, appMap.get(tenant.applicationId).get, tenant.id)
      }
    }

  }

}

case class TenantData(name: String, apps: Seq[Application], lang: Language, langs: Seq[Language], application: Application, id: Tenant.Id)

