package annette.core.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{ Directives, Route }
import annette.core.AnnetteException
import annette.core.akkaext.http.PaginationDirectives
import annette.core.domain.tenancy.TenantManager
import annette.core.domain.tenancy.model._
import annette.core.security.SecurityDirectives
import annette.core.security.authentication.Session
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait TenantRoutes extends Directives with PaginationDirectives {
  implicit val c: ExecutionContext
  val annetteSecurityDirectives: SecurityDirectives
  val tenantManager: TenantManager
  val authorizationManager: ActorRef
  val config: Config

  import FailFastCirceSupport._
  import annetteSecurityDirectives._
  import io.circe.generic.auto._

  def createTenant(implicit session: Session): Route = (post & entity(as[CreateTenant])) { x =>
    //complete(tenantManager.create(x))
    ???
  }

  def getTenant(implicit session: Session): Route = (path(JavaUUID) & get) { tenantId =>
    ???
  }

  def updateTenant(implicit session: Session): Route = (path(JavaUUID) & post) { tenantId =>
    ???
  }

  def deleteTenant(implicit session: Session): Route = (path(JavaUUID) & delete) { tenantId =>
    //complete(tenantManager.delete(tenantId))
    ???
  }

  def listTenants(implicit session: Session): Route = (get & pagination) { page =>
    println(page)
    val ff = for {
      f <- tenantManager.paginateListTenants(page)
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

  val tenantRoutes: Route = (pathPrefix("tenant") & authorized) { implicit session =>
    createTenant ~ getTenant ~ updateTenant ~ deleteTenant ~ listTenants
  }
}
