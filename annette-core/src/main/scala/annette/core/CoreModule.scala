package annette.core

import javax.inject.{Inject, Named, Singleton}
import akka.actor.{ActorRef, ActorSystem}
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import akka.util.Timeout
import annette.core.security.authentication.{AuthenticationService, Session}
import annette.core.domain.DB
import annette.core.domain.application.dao.{ApplicationDao, ApplicationDb}
import annette.core.domain.language.dao.{LanguageDao, LanguageDb}
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao._
import annette.core.http.ExceptionHandler
import annette.core.http.routes.{ApiRoutes, AuthRoutes, UsersRoutes}
import annette.core.security.AnnetteSecurityDirectives
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

@Singleton
class CoreModule @Inject() (
                             implicit val system: ActorSystem,
                             val config: Config,
                             val db: DB,
                             val tenancyDb: TenancyDb,
                             val userDao: UserService,
                             val tenantDao: TenantDao,
                             val tenantUserDao: TenantUserDao,
                             val languageDb: LanguageDb,
                             val languageDao: LanguageDao,
                             val applicationDb: ApplicationDb,
                             val applicationDao: ApplicationDao,
                             val tenantUserRoleDao: TenantUserRoleDao,
                             val sessionDao: SessionDao,
                             val annetteSecurityDirectives: AnnetteSecurityDirectives,
                             @Named(AuthenticationService.name) val authenticationService: ActorRef,
  ) extends AnnetteHttpModule with ApiRoutes with ExceptionHandler {
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
    exceptionHandler = exceptionHandler(log)
  )
}
