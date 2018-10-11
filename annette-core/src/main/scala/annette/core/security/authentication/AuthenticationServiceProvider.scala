package annette.core.security.authentication

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.Cluster
import akka.event.{ LogSource, Logging }
import akka.routing.FromConfig
import akka.util.Timeout
import annette.core.domain.InitCoreTables
import annette.core.domain.application.ApplicationManager
import annette.core.domain.language.LanguageManager
import annette.core.domain.tenancy.{ SessionManager, TenantService, UserManager }
import com.google.inject.Provider
import com.typesafe.config.Config
import javax.inject._

import scala.concurrent.ExecutionContext

@Singleton
class AuthenticationServiceProvider @Inject() (
  system: ActorSystem,
  sessionManager: SessionManager,
  TenantService: TenantService,
  applicationManager: ApplicationManager,
  userManager: UserManager,
  languageManager: LanguageManager,
  config: Config,
  initCoreTables: InitCoreTables)(implicit c: ExecutionContext, t: Timeout) extends Provider[ActorRef] {

  implicit val myLogSourceType: LogSource[AuthenticationServiceProvider] = (a: AuthenticationServiceProvider) => "AuthenticationServiceProvider"

  val log = Logging(system, this)

  log.debug("AuthenticationServiceProvider")

  val clusterMode = config.getBoolean("annette.cluster")
  final val routerName = "AuthenticationManagerRouter"

  private val authenticationService: ActorRef = initAuthenticationService()

  def get = authenticationService

  private def initAuthenticationService() = {
    if (clusterMode) initClusterMode()
    else initSingleInstanceMode()
  }

  private def initClusterMode() = {
    log.debug("InitAuthenticationService: Cluster Mode")
    val cluster = Cluster(system)
    if (cluster.selfRoles.contains("core")) {
      initService()
    }
    initClusterServiceRouter()
  }

  private def initSingleInstanceMode() = {
    log.info("InitAuthenticationService: Single Instance Mode")
    initService()
  }

  private def initService() = system.actorOf(
    props = AuthenticationService.props(
      sessionManager = sessionManager,
      TenantService = TenantService,
      applicationManager = applicationManager,
      userManager = userManager,
      languageManager = languageManager,
      config = config),
    name = AuthenticationService.name)

  private def initClusterServiceRouter() = {
    system.actorOf(FromConfig.props(Props.empty), routerName)
  }

}
