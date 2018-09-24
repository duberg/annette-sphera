package annette.core

import javax.inject.{Inject, Named, Singleton}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import annette.core.domain.DB
import annette.core.domain.application.dao.{ApplicationDao, ApplicationDb}
import annette.core.domain.language.dao.{LanguageDao, LanguageDb}
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao._
import annette.core.http.routes.{AuthRoutes, UserRoutes}
import annette.core.http.security.AnnetteSecurityDirectives
import annette.core.modularize.AnnetteHttpModule
import annette.core.services.authentication.AuthenticationService
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class CoreModule @Inject() (
                             val system: ActorSystem,
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
  ) extends AnnetteHttpModule with AuthRoutes with UserRoutes {
  System.setProperty("logback.configurationFile", "conf/logback.xml")

  implicit val sys: ActorSystem = system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val c: ExecutionContextExecutor = system.dispatcher

  override def name: String = "annette-core"

  override def buildInfo = BuildInfo.toString

  override def init(): Future[Unit] = Future.successful()

  //implicit val routingSettings: RoutingSettings = RoutingSettings(config)

  override def routes = authRoutes ~ userRoutes
}
