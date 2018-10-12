package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.{ Directives, Route }
import akka.pattern.AskSupport
import annette.core.AnnetteException
import annette.core.akkaext.http.PaginationDirectives
import annette.core.domain.language.LanguageManager
import annette.core.domain.tenancy.{ TenantService, UserManager }
import annette.core.notification._
import annette.core.security.SecurityDirectives
import annette.core.security.authorization.AuthorizationActor.ReadPolicy
import annette.core.security.authorization.AuthrReqUser
import annette.core.utils.Generator
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait AuthorizationRoutes extends Directives
  with PaginationDirectives
  with AskSupport
  with Generator {
  val TenantService: TenantService
  val userManager: UserManager
  val languageManager: LanguageManager
  val authenticationManager: ActorRef
  val annetteSecurityDirectives: SecurityDirectives
  val notificationManager: NotificationManager
  val authorizationManager: ActorRef
  val apiUrl: String

  implicit val c: ExecutionContext
  //implicit val t: Timeout = 30.seconds // TODO: заменить на конфигурацию

  import FailFastCirceSupport._
  import annetteSecurityDirectives._
  import io.circe.generic.auto._

  //  def create: Route =
  //    ignoreTrailingSlash {
  //      (post & entity(as[AuthrPolicy])) { policy: AuthrPolicy =>
  //        onComplete(authorizationManager.ask(CreatePolicy(policy.roleName, policy.accessPath, policy.actions))) {
  //          case Success(_) => complete(StatusCodes.Created, policy.roleName)
  //          case Failure(e) => complete(StatusCodes.InternalServerError, "error message: ${e.getMessage}")
  //        }
  //      }
  //    }

  case class Permission(id: String, accessPath: String, action: String)
  case class PaginatePermissionsList(items: List[Permission], totalCount: Int)

  def listPermissions: Route = (get & pagination) { page =>
    onComplete(authorizationManager.ask(ReadPolicy(None, None))
      .mapTo[Seq[AuthrReqUser]]
      .map(x => PaginatePermissionsList(x.map(y => Permission(
        id = y.userId,
        accessPath = y.accessPath,
        action = y.action)).toList, x.size))) {
      case Success(result) => result match {
        case result => complete(result)
        case _ =>
          throw new AnnetteException(s"Not found permission")
      }
      case Failure(e) => throw new AnnetteException(s"error message: ${e.getMessage}")
    }
  }

  def authorizationRoutes = pathPrefix("permissions") {
    listPermissions
  }
}