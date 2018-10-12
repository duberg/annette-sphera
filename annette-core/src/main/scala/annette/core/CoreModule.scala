package annette.core

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import annette.core.domain.application.ApplicationService
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.{ SessionService, TenantService, UserService }
import annette.core.http.ExceptionHandler
import annette.core.http.routes.ApiRoutes
import annette.core.notification.NotificationService
import annette.core.security.SecurityDirectives
import annette.core.security.authentication.AuthenticationService
import com.typesafe.config.Config
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class CoreModule @Inject() (
  implicit
  val system: ActorSystem,
  val config: Config,
  val userService: UserService,
  val tenantService: TenantService,
  val languageService: LanguageService,
  val applicationService: ApplicationService,
  val sessionService: SessionService,
  val annetteSecurityDirectives: SecurityDirectives,
  val notificationService: NotificationService,
  @Named(AuthenticationService.name) val authenticationService: ActorRef,
  @Named("AuthorizationManager") val authorizationService: ActorRef) extends AnnetteHttpModule with ApiRoutes with ExceptionHandler {
  System.setProperty("logback.configurationFile", "conf/logback.xml")

  implicit val c: ExecutionContext = system.dispatcher
  implicit val t: Timeout = 10.seconds
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = system.log

  def name: String = "annette-core"
  def buildInfo: String = BuildInfo.toString
  def init(): Future[Unit] = Future.successful()

  def routes: Route = Route.seal(apiRoutes)(
    routingSettings = RoutingSettings(config),
    exceptionHandler = exceptionHandler(log))
}
