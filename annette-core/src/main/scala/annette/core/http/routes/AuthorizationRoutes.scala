package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport
import annette.core.domain.application.Application
import annette.core.domain.language.dao.LanguageDao
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.UserManager
import annette.core.domain.tenancy.dao.{ TenantDao, TenantUserDao, TenantUserRoleDao }
import annette.core.domain.tenancy.model._
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.security.authentication.{ ApplicationState, AuthenticationService, ForbiddenException }
import annette.core.security.authorization.AuthorizationActor.{ CreatePolicy, ReadPolicy }
import annette.core.security.authorization.{ AuthrPolicy, AuthrReqUser }
import annette.core.security.verification.CreateEmailVerification
import annette.core.utils.Generator
import annette.core.{ AnnetteException, RequiredValueNotProvided, TenantNotFoundException }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait AuthorizationRoutes extends Directives with AskSupport with Generator {
  val tenantDao: TenantDao
  val tenantUserDao: TenantUserDao
  val tenantUserRoleDao: TenantUserRoleDao
  val userManager: UserManager
  val languageDao: LanguageDao
  val authenticationService: ActorRef
  val annetteSecurityDirectives: SecurityDirectives
  val notificationManager: NotificationManager
  val authorizationManager: ActorRef
  val apiUrl: String

  implicit val c: ExecutionContext
  //implicit val t: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import annetteSecurityDirectives._
  import io.circe.generic.auto._

  def create: Route =
    ignoreTrailingSlash {
      (post & entity(as[AuthrPolicy])) { policy: AuthrPolicy =>
        onComplete(authorizationManager.ask(CreatePolicy(policy.roleName, policy.accessPath, policy.actions))) {
          case Success(_) => complete(StatusCodes.Created, policy.roleName)
          case Failure(e) => complete(StatusCodes.InternalServerError, "error message: ${e.getMessage}")
        }
      }
    }

  def listRoles: Route = (path("list") & get) {
    parameters('curr_page.as[Int].?, 'page_size.as[Int].?, 'search_name.as[String].?, 'order.as[String].?) { (currPage, pageSize, search_name, order) =>
      onComplete(authorizationManager.ask(ReadPolicy(search_name, order)).mapTo[Seq[AuthrReqUser]]) {
        case Success(result) => result match {
          case result: Seq[AuthrReqUser] => complete(result)
          case _ =>
            throw new AnnetteException(s"Not found role $search_name")
        }
        case Failure(e) => throw new AnnetteException(s"error message: ${e.getMessage}")
      }

    }
  }

  def authorizationRoutes = pathPrefix("roles" / "permissions") {
    create ~ listRoles
  }
}