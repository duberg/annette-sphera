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
package annette.core.http.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.application.model.Application
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.Tenant
import annette.core.exception.AnnetteException
import annette.core.http.security.AnnetteSecurityDirectives
import annette.core.services.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class AuthApi(
  languageDao: LanguageDao,
  authenticationService: ActorRef,
  annetteSecurityDirectives: AnnetteSecurityDirectives,
  config: Config)(implicit ec: ExecutionContextExecutor) {

  implicit val serviceTimeout: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import annetteSecurityDirectives._
  import io.circe.generic.auto._

  private def loginRoutes = (path("login") & post) {
    (entity(as[AuthenticationService.LoginData]) & extractClientIP) {
      (loginData, clientIp) =>
        val future = authenticationService
          .ask(AuthenticationService.Login(loginData, clientIp.toOption.map(_.toString).getOrElse("")))
        onComplete(future) {
          case Success(response) =>
            response match {
              case applicationState: ApplicationState =>
                complete(applicationState)
              case utd: AuthenticationService.UserTenantData =>
                complete(StatusCodes.RetryWith, utd)
              case AuthenticationService.FailureResponse(th) =>
                if (th.isInstanceOf[AnnetteException]) {
                  val annetteException = th.asInstanceOf[AnnetteException]
                  complete(StatusCodes.Unauthorized, annetteException.exceptionMessage)
                } else {
                  complete(StatusCodes.Unauthorized)
                }
            }
          case Failure(th) =>
            th.printStackTrace()
            complete(StatusCodes.Unauthorized)
        }
    }
  }

  private def logoutRoutes = path("logout") {
    post {
      complete(StatusCodes.NotImplemented)
    }
    get {
      maybeAuthenticated {
        case Some(sessionData) =>
          val logoutFuture = authenticationService
            .ask(AuthenticationService.Logout(sessionData.sessionId))
          onComplete(logoutFuture) {
            _ =>
              redirect("/auth/login", StatusCodes.TemporaryRedirect)

          }
        case None =>
          redirect("/auth/login", StatusCodes.TemporaryRedirect)
      }
    }
  }

  private def applicationStateRoutes = path("applicationState") {
    get {
      maybeAuthenticated {
        maybeSession =>
          import FailFastCirceSupport._
          import io.circe.generic.auto._
          val applicationStateFuture = authenticationService
            .ask(AuthenticationService.GetApplicationState(maybeSession))
            .mapTo[ApplicationState]
          onComplete(applicationStateFuture) {
            case Success(applicationState) =>
              complete(applicationState)
            case Failure(throwable) =>
              throwable match {
                case annetteException: AnnetteException =>
                  complete(StatusCodes.InternalServerError, annetteException.exceptionMessage)
                case _ =>
                  complete(StatusCodes.InternalServerError, Map("code" -> throwable.getMessage))
              }
          }
      }

    } ~
      (post & authenticated & entity(as[SetApplicationState])) {
        case (sessionData, SetApplicationState(tenantId, applicationId, languageId)) =>
          import FailFastCirceSupport._
          import io.circe.generic.auto._
          val applicationStateFuture = authenticationService
            .ask(AuthenticationService.SetApplicationState(sessionData, tenantId, applicationId, languageId))
            .mapTo[ApplicationState]
          onComplete(applicationStateFuture) {
            case Success(applicationState) =>
              complete(applicationState)
            case Failure(throwable) =>
              throwable match {
                case annetteException: ForbiddenException =>
                  complete(StatusCodes.Forbidden, annetteException.exceptionMessage)
                case annetteException: AnnetteException =>
                  complete(StatusCodes.InternalServerError, annetteException.exceptionMessage)
                case _ =>
                  complete(StatusCodes.InternalServerError, Map("code" -> throwable.getMessage))
              }
          }

      }
  }

  private def languagesRoute = (get & path("languages")) {
    onComplete(languageDao.selectAll) {
      languages =>
        complete(languages)
    }
  }

  private def heartbeatRoute = (put & path("heartbeat" / Segment) & authenticated) {
    case (live, sessionData) =>
      import FailFastCirceSupport._
      if (live == "true") {
        val applicationStateFuture = authenticationService
          .ask(AuthenticationService.UpdateLastOpTimestamp(sessionData.sessionId))
      }
      complete(true)
  }

  def routes = pathPrefix("auth" / "api") {
    implicit val routingSettings = RoutingSettings(config)
    Route.seal(
      loginRoutes ~ logoutRoutes ~ applicationStateRoutes ~ heartbeatRoute ~ languagesRoute)
  }

}

case class SetApplicationState(
  tenantId: Tenant.Id,
  applicationId: Application.Id,
  languageId: Language.Id)