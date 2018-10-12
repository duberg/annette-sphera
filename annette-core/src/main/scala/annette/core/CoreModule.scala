package annette.core

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import annette.core.domain.application.ApplicationManager
import annette.core.domain.language.LanguageManager
import annette.core.domain.tenancy.{ SessionManager, TenantService, UserManager }
import annette.core.http.ExceptionHandler
import annette.core.http.routes.ApiRoutes
import annette.core.notification.NotificationManager
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
  val userManager: UserManager,
  val TenantService: TenantService,
  val languageManager: LanguageManager,
  val applicationManager: ApplicationManager,
  val sessionManager: SessionManager,
  val annetteSecurityDirectives: SecurityDirectives,
  val notificationManager: NotificationManager,
  @Named(AuthenticationService.name) val authenticationManager: ActorRef,
  @Named("AuthorizationManager") val authorizationManager: ActorRef) extends AnnetteHttpModule with ApiRoutes with ExceptionHandler {
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
