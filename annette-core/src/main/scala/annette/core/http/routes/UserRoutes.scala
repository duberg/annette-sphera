package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.{ Directives, Route }
import annette.core.akkaext.http.PaginationDirectives
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ TenantService, UserService }
import annette.core.json._
import annette.core.security.SecurityDirectives
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

trait UserRoutes extends Directives with PaginationDirectives {
  implicit val c: ExecutionContext

  val annetteSecurityDirectives: SecurityDirectives
  val userService: UserService
  val tenantService: TenantService
  val authorizationService: ActorRef
  val config: Config

  import annetteSecurityDirectives._

  def createUser: Route = (post & entity(as[CreateUser])) { x =>
    complete(userService.createUser(x))
  }

  def getUserById: Route = (path(JavaUUID) & get) { userId =>
    complete(userService.getUserById(userId))
  }

  def listUsers: Route = (get & pagination) { page =>
    complete(userService.paginateListUsers(page))
  }

  def updateUser: Route = (path(JavaUUID) & post & entity(as[UpdateUser])) { (_, x) =>
    complete(userService.updateUser(x))
  }

  def deleteUser: Route = (path(JavaUUID) & delete) { x =>
    complete(userService.deleteUser(x))
  }

  val userRoutes: Route = (pathPrefix("users") & authorized) { implicit session =>
    getUserById ~ listUsers ~ createUser ~ updateUser ~ deleteUser
  }
}
