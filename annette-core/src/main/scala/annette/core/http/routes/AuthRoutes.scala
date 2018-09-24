package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.settings.RoutingSettings
import akka.pattern.AskSupport
import akka.util.Timeout
import annette.core.domain.application.model.Application
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.model.{ CreateUser, Tenant, UpdateUser }
import annette.core.exception.AnnetteException
import annette.core.http.security.AnnetteSecurityDirectives
import annette.core.services.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.java8.time.TimeInstances

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
import scala.concurrent.duration._

trait AuthRoutes extends Directives with AskSupport with TimeInstances {
  val userDao: UserService
  val languageDao: LanguageDao
  val authenticationService: ActorRef
  val annetteSecurityDirectives: AnnetteSecurityDirectives
  val config: Config
  implicit val c: ExecutionContext
  //implicit val t: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.java8.time.TimeInstances

  import annetteSecurityDirectives._

  case class SetApplicationState(
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)

  def login: Route = (path("login") & post) {
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

  def logout: Route = (path("logout") & post & authOpt) {
    case Some(sessionData) =>
      complete(authenticationService
        .ask(AuthenticationService.Logout(sessionData.sessionId))
        .mapTo[AuthenticationService.Response])
    case None =>
      complete("logged out")
  }

  def register: Route = {

    (path("register") & post & entity(as[CreateUser])) { x =>
      complete(userDao.create(x))
    } ~ (path("register") & post)(complete("ff"))
  }

  private def applicationStateRoutes = path("applicationState") {
    get {
      authOpt {
        maybeSession =>

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

  def authRoutes = pathPrefix("auth") {
    login ~ logout ~ register ~ applicationStateRoutes ~ heartbeatRoute ~ languagesRoute
  }

}
