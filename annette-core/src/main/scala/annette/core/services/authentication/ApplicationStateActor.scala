/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */

package annette.core.services.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import akka.pattern.pipe
import annette.core.domain.application.dao.ApplicationDao
import annette.core.domain.application.model.Application
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.dao.{ SessionDao, TenantDao, TenantUserDao, UserDao }
import annette.core.domain.tenancy.model.Tenant
import annette.core.services.authentication.jwt.JwtHelper

import scala.concurrent.Future

/**
 * Created by valery on 22.10.16.
 */
class ApplicationStateActor(
  sessionDao: SessionDao,
  tenantDao: TenantDao,
  applicationDao: ApplicationDao,
  userDao: UserDao,
  tenantUserDao: TenantUserDao,
  languageDao: LanguageDao,
  override val secret: String)
  extends Actor with ActorLogging with JwtHelper {

  implicit val ec = context.dispatcher

  override def receive: Receive = LoggingReceive {

    case AuthenticationService.GetApplicationState(maybeSessionData) =>
      getApplicationState(maybeSessionData) pipeTo sender

    case AuthenticationService.SetApplicationState(sessionData, tenantId, applicationId, languageId) =>
      setApplicationState(sessionData, tenantId, applicationId, languageId) pipeTo sender
  }

  def getApplicationState(maybeSessionData: Option[SessionData]): Future[ApplicationState] = {
    maybeSessionData
      .map {
        sessionData =>
          for {
            userOpt <- userDao.getById(sessionData.userId)
            tenantOpt <- tenantDao.getById(sessionData.tenantId)
            applicationOpt <- applicationDao.getById(sessionData.applicationId)
            languages <- languageDao.selectAll
            userTenantData <- tenantUserDao.getUserTenantData(sessionData.userId)
          } yield {
            val language = languages.find(_.id == sessionData.languageId).getOrElse(languages.head)
            val jwtToken = encodeSessionData(sessionData)
            ApplicationState(
              authenticated = true,
              language = language,
              languages = languages.toSeq,
              user = userOpt,
              tenant = tenantOpt,
              application = applicationOpt,
              tenantData = userTenantData,
              jwtToken = Some(jwtToken))
          }
      }
      .getOrElse {
        for {
          languages <- languageDao.selectAll
        } yield {
          val language = languages.find(_.id == "RU").getOrElse(languages.head) // TODO: заменить константу на настройку
          ApplicationState(
            authenticated = false,
            language = language,
            languages = languages.toSeq)
        }
      }
  }

  def setApplicationState(
    sessionData: SessionData,
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id): Future[ApplicationState] = {
    for {
      userOpt <- userDao.getById(sessionData.userId)
      tenantOpt <- tenantDao.getById(tenantId)
      applicationOpt <- applicationDao.getById(applicationId)
      languages <- languageDao.selectAll
      userTenantData <- tenantUserDao.getUserTenantData(sessionData.userId)
    } yield {
      val languageOpt = languages.find(_.id == languageId)

      if (tenantOpt.isEmpty) throw new TenantNotFoundException
      val tenantDataOpt = userTenantData.find(td => td.id == tenantId)
      if (tenantDataOpt.isEmpty) throw new UserNotAssignedToTenantException
      if (applicationOpt.isEmpty) throw new ApplicationNotFoundException
      if (!tenantOpt.get.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException
      if (languageOpt.isEmpty) throw new LanguageNotFoundException
      if (!tenantOpt.get.languages.contains(languageId)) throw new LanguageNotAssignedToTenantException

      val jwtToken = encodeSessionData(sessionData.copy(tenantId = tenantId, applicationId = applicationId, languageId = languageId))

      sessionDao.updateTenantApplicationLanguage(sessionData.sessionId, tenantId, applicationId, languageId)

      ApplicationState(
        authenticated = true,
        language = languageOpt.get,
        languages = languages,
        user = userOpt,
        tenant = tenantOpt,
        application = applicationOpt,
        tenantData = userTenantData,
        jwtToken = Some(jwtToken))
    }

  }

}