package annette.app

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import annette.app.exchange.ExchangeService
import annette.app.http.routes.ExchangeRoutes
import annette.core.http.ExceptionHandler
import annette.core.security.SecurityDirectives
import annette.core.security.authentication.AuthenticationService
import annette.core.{ AnnetteHttpModule, BuildInfo }
import com.typesafe.config.Config
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AppModule @Inject() (
  implicit
  val system: ActorSystem,
  val config: Config,
  val exchangeService: ExchangeService,
  val annetteSecurityDirectives: SecurityDirectives,
  @Named(AuthenticationService.name) val authenticationService: ActorRef,
  @Named("AuthorizationManager") val authorizationService: ActorRef) extends AnnetteHttpModule with ExchangeRoutes with ExceptionHandler {

  implicit val c: ExecutionContext = system.dispatcher
  implicit val t: Timeout = 10.seconds
  implicit val m: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = system.log

  def name: String = "annette-core"
  def buildInfo: String = BuildInfo.toString
  def init(): Future[Unit] = Future.successful()

  def routes: Route = Route.seal(exchangeRoutes)(
    routingSettings = RoutingSettings(config),
    exceptionHandler = exceptionHandler(log))
}