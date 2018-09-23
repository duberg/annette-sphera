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
import annette.core.domain.application.dao.ApplicationDao
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao.{ SessionDao, TenantDao, TenantUserDao }
import annette.core.domain.tenancy.model.OpenSession
import annette.core.services.authentication.jwt.JwtHelper

import scala.concurrent.Future

/**
 * Created by valery on 22.10.16.
 */
class AuthenticationActor(
  sessionDao: SessionDao,
  tenantDao: TenantDao,
  applicationDao: ApplicationDao,
  userDao: UserService,
  tenantUserDao: TenantUserDao,
  languageDao: LanguageDao,
  override val secret: String)
  extends Actor with ActorLogging with JwtHelper {

  implicit val ec = context.dispatcher

  override def receive: Receive = LoggingReceive {
    case AuthenticationService.Authenticate(jwtToken) =>
      authenticate(jwtToken)
  }

  private def authenticate(jwtToken: String) = {
    val requestor = sender
    decodeSessionData(jwtToken)
      .map {
        sessionData =>
          for {
            openSession <- validateSession(sessionData)
            //_ <- validateAssignments(openSession, sessionData) // TODO: а надо ли валидировать присвоения???
          } yield {
            AuthenticationService.Authenticated(sessionData)
          }
      }
      .fold(requestor ! AuthenticationService.AuthenticationFailed()) {
        future =>
          future.foreach { result => requestor ! result }
          future.failed.foreach { _ => requestor ! AuthenticationService.AuthenticationFailed() }
      }
  }
  private def validateSession(sessionData: SessionData): Future[OpenSession] = {
    for {
      openSessionOpt <- sessionDao.getOpenSessionById(sessionData.sessionId)
    } yield {
      openSessionOpt.fold(throw new AuthenticationFailedException()) {
        openSession =>
          if (sessionData.userId != openSession.userId) {
            throw new AuthenticationFailedException()
          }
          openSession
      }
    }
  }

  private def validateAssignments(session: OpenSession, sessionData: SessionData) = {
    val tenantId = sessionData.tenantId
    val applicationId = sessionData.applicationId
    for {
      userOpt <- userDao.getById(session.userId)
      tenantOpt <- tenantDao.getById(tenantId)
      applicationOpt <- applicationDao.getById(applicationId)
      tenantUserExist <- tenantUserDao.isExist(tenantId, session.userId)
    } yield {
      if (userOpt.isEmpty) throw new UserNotFoundException()
      if (tenantOpt.isEmpty) throw new TenantNotFoundException()
      if (applicationOpt.isEmpty) throw new ApplicationNotFoundException()
      if (!tenantUserExist) throw new UserNotAssignedToTenantException()
      val tenant = tenantOpt.get
      if (!tenant.applications.contains(applicationId)) throw new ApplicationNotAssignedToTenantException
      (tenantId, applicationId)
    }
  }

}