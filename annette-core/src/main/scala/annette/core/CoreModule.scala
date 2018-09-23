package annette.core

import javax.inject.{Inject, Named, Singleton}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import annette.core.domain.DB
import annette.core.domain.application.dao.{ApplicationDao, ApplicationDb}
import annette.core.domain.language.dao.{LanguageDao, LanguageDb}
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.dao._
import annette.core.http.api.AuthApi
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
  ) extends AnnetteHttpModule {
  System.setProperty("logback.configurationFile", "conf/logback.xml")

  implicit val sys = system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  override def name: String = "annette-core"

  override def buildInfo = BuildInfo.toString

  override def init(): Future[Unit] = Future.successful()

  private val authApi = new AuthApi(
    languageDao = languageDao,
    authenticationService = authenticationService,
    annetteSecurityDirectives = annetteSecurityDirectives,
    config = config
  )
  override def routes = authApi.routes
}
