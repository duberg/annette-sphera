package annette.app.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.{ Directives, Route }
import annette.app.exchange.Exchange.Convert
import annette.core.akkaext.http.PaginationDirectives
import annette.core.domain.tenancy.{ TenantService, UserService }
import annette.core.domain.tenancy.model.{ CreateUser, UpdateUser }
import annette.core.security.SecurityDirectives
import annette.app.exchange.{ ExchangeService }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import annette.core.json._

trait ExchangeRoutes extends Directives {
  //implicit val c: ExecutionContext

  //val annetteSecurityDirectives: SecurityDirectives
  val exchangeService: ExchangeService

  //import annetteSecurityDirectives._

  def getRates: Route = get {
    complete(exchangeService.getRates)
  }

  def convert: Route = (post & entity(as[Convert])) { x =>
    complete(exchangeService.convert(x))
  }

  // todo: add authorized directive
  val exchangeRoutes: Route = pathPrefix("exchange" /* & authorized */ ) {
    getRates ~ convert
  }
}