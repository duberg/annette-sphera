package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport
import annette.core.akkaext.http.PaginationDirectives
import annette.core.domain.application.Application
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ SessionService, TenantService, UserService }
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.security.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import annette.core.security.verification.CreateEmailVerification
import annette.core.utils.Generator
import annette.core.{ AnnetteException, RequiredValueNotProvided, TenantNotFoundException }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait AuthenticationRoutes extends Directives
  with AskSupport
  with Generator
  with PaginationDirectives {
  val tenantService: TenantService
  val userService: UserService
  val languageService: LanguageService
  val authenticationService: ActorRef
  val sessionService: SessionService
  val annetteSecurityDirectives: SecurityDirectives
  val notificationService: NotificationService
  val apiUrl: String

  implicit val c: ExecutionContext
  //implicit val t: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import annetteSecurityDirectives._
  import io.circe.generic.auto._

  case class SetApplicationState(
    tenantId: Tenant.Id,
    applicationId: Application.Id,
    languageId: Language.Id)

  def signIn: Route = (path("signin") & post) {
    (entity(as[AuthenticationService.Credentials]) & extractClientIP) {
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

  def signOut: Route = (path("signout") & post & authenticatedOpt) {
    case Some(sessionData) =>
      complete(authenticationService
        .ask(AuthenticationService.Logout(sessionData.sessionId))
        .mapTo[AuthenticationService.Response])
    case None =>
      complete("signout")
  }

  def signUp: Route = (path("signup") & post & entity(as[SignUpUser])) { x =>
    def f = tenantService.listTenantsIds.flatMap(f = tenantIds => {
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
          roles = Some(x.tenants.map(_ -> Set("user")).toMap),

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
          meta = None,
          status = Some(0))

        val code = generateUUIDStr

        val x2 = CreateEmailVerification(
          code = code,
          email = x.email,
          duration = 10.minutes)

        for {
          user <- userService.createUser(x1)
          verification <- notificationService.createVerification(x2)
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

          notificationService.push(c3)

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
    onComplete(languageService.selectAll) {
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

  def listOpenSessions = (path("sessions") & get & pagination) { page =>
    val ff = for {
      f <- sessionService.paginateOpenSessions(page)
      //r <- tenantUserRoleDao.selectAll
    } yield f

    onComplete(ff) {
      case Success(x) => complete(x)
      case Success(_) => complete(StatusCodes.InternalServerError)
      case Failure(throwable) =>
        throwable match {
          case annetteException: AnnetteException =>
            complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
          case _ =>
            complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
        }
    }
  }

  def authenticationRoutes = pathPrefix("auth") {
    signIn ~
      signOut ~
      signUp ~
      applicationStateRoutes ~
      heartbeatRoute ~
      languagesRoute ~
      listOpenSessions
  }

}
