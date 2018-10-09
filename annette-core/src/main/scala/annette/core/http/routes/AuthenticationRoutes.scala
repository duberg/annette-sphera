package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directives, Route }
import akka.http.scaladsl.settings.RoutingSettings
import akka.pattern.AskSupport
import akka.util.Timeout
import annette.core.security.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import annette.core.{ AnnetteException, RequiredValueNotProvided, TenantNotFoundException }
import annette.core.domain.application.Application
import annette.core.domain.language.LanguageManager
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.{ TenantManager, UserManager }
import annette.core.domain.tenancy.model._
import annette.core.model.EntityType.Verification
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.security.verification.CreateEmailVerification
import annette.core.utils.Generator
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.concurrent.duration._

trait AuthenticationRoutes extends Directives with AskSupport with Generator {
  val tenantManager: TenantManager
  val userManager: UserManager
  val languageManager: LanguageManager
  val authenticationManager: ActorRef
  val annetteSecurityDirectives: SecurityDirectives
  val notificationManager: NotificationManager
  val apiUrl: String

  implicit val c: ExecutionContext
  //implicit val t: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import io.circe.generic.auto._

  import annetteSecurityDirectives._

  case class SetApplicationState(
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)

  def signIn: Route = (path("signin") & post) {
    (entity(as[AuthenticationService.Credentials]) & extractClientIP) {
      (loginData, clientIp) =>
        val future = authenticationManager
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

  def signOut: Route = (path("signout") & post & authenticatedOpt) {
    case Some(sessionData) =>
      complete(authenticationManager
        .ask(AuthenticationService.Logout(sessionData.sessionId))
        .mapTo[AuthenticationService.Response])
    case None =>
      complete("signout")
  }

  def signUp: Route = (path("signup") & post & entity(as[SignUpUser])) { x =>
    def f = tenantManager.listTenantsIds.flatMap(f = tenantIds => {
      // if empty tenants
      if (x.tenants.isEmpty) throw RequiredValueNotProvided("tenants")

      val unknownTenants = x.tenants diff tenantIds

      // if unknown tenants
      if (unknownTenants.nonEmpty) throw TenantNotFoundException(unknownTenants)

      else {
        val x1 = CreateUser(
          username = None,
          displayName = Some(s"${x.lastName} ${x.firstName}"),
          firstName = x.firstName,
          lastName = x.lastName,
          middleName = None,
          gender = None,

          // Set default role "user"
          roles = x.tenants.map(_ -> Set("user")).toMap,

          email = Some(x.email),
          url = None,
          description = None,
          phone = None,
          language = None,
          password = x.password,
          avatarUrl = None,
          sphere = None,
          company = None,
          position = None,
          rank = None,
          additionalTel = None,
          additionalMail = None,
          meta = Map.empty,
          status = 0)

        val code = generateUUIDStr

        val x2 = CreateEmailVerification(
          code = code,
          email = x.email,
          duration = 10.minutes)

        for {
          user <- userManager.create(x1)
          verification <- notificationManager.createVerification(x2)
        } yield {
          /**
           * = Email verification =
           */
          val url = s"$apiUrl/verification/${verification.id}/$code"
          val template = html.ConfirmationEmail(url)
          val c3 = CreateVerifyByEmailNotification(
            email = x.email,
            subject = "Confirm your email address",
            message = template.toString(),
            code = code)

          notificationManager.push(c3)

          user
        }
      }
    })
    complete(f)
  }

  private def applicationStateRoutes = path("applicationState") {
    get {
      authenticatedOpt {
        maybeSession =>

          val applicationStateFuture = authenticationManager
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

          val applicationStateFuture = authenticationManager
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
    onComplete(languageManager.selectAll) {
      languages =>
        complete(languages)
    }
  }

  private def heartbeatRoute = (put & path("heartbeat" / Segment) & authenticated) {
    case (live, sessionData) =>
      import FailFastCirceSupport._
      if (live == "true") {
        val applicationStateFuture = authenticationManager
          .ask(AuthenticationService.UpdateLastOpTimestamp(sessionData.sessionId))
      }
      complete(true)
  }

  def authenticationRoutes = pathPrefix("auth") {
    signIn ~ signOut ~ signUp ~ applicationStateRoutes ~ heartbeatRoute ~ languagesRoute
  }

}
