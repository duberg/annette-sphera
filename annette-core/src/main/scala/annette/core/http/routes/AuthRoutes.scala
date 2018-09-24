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
import annette.core.domain.tenancy.dao.{ TenantDao, TenantUserDao, TenantUserRoleDao }
import annette.core.domain.tenancy.model._
import annette.core.exception.AnnetteException
import annette.core.http.security.AnnetteSecurityDirectives
import annette.core.services.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.java8.time.TimeInstances

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.concurrent.duration._

trait AuthRoutes extends Directives with AskSupport with TimeInstances {
  val tenantDao: TenantDao
  val tenantUserDao: TenantUserDao
  val tenantUserRoleDao: TenantUserRoleDao
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

  def signIn: Route = (path("signin") & post) {
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

  def signOut: Route = (path("signout") & post & authOpt) {
    case Some(sessionData) =>
      complete(authenticationService
        .ask(AuthenticationService.Logout(sessionData.sessionId))
        .mapTo[AuthenticationService.Response])
    case None =>
      complete("signout")
  }

  def signUp: Route = (path("signup") & post & entity(as[SignUpUser])) { x =>
    def createUser = tenantDao.listIds.flatMap(tenantIds => {
      if (x.tenants.isEmpty) throw new AnnetteException(s"empty tent")
      val unknownTenants = (Set[Tenant.Id]() /: x.tenants)({
        case (acc, tenantId) if x.tenants.contains(tenantId) => acc
        case (acc, tenantId) => acc + tenantId
      })
      if (unknownTenants.nonEmpty) throw new AnnetteException(s"$unknownTenants")
      else {
        // todo: add email verification (aka account activation)
        val createUser = CreateUser(
          username = None,
          displayName = Some(s"${x.lastName} ${x.firstName}"),
          firstName = x.firstName,
          lastName = x.lastName,
          middleName = None,
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
          deactivated = true)

        for {
          x1 <- userDao.create(createUser)
          x2 <- Future.sequence(x.tenants.map(tenantUserDao.create(_, x1.id)))
          x3 <- Future.sequence(x.tenants.map(tenantId => tenantUserRoleDao.store(TenantUserRole(tenantId, x1.id, Set("user")))))
        } yield x1
      }
    })
    complete(createUser)
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
    signIn ~ signOut ~ signUp ~ applicationStateRoutes ~ heartbeatRoute ~ languagesRoute
  }

}
