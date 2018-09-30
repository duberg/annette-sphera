package annette.core.inject

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.domain.{ CoreService, InitCoreTables }
import annette.core.security.authentication.{ AuthenticationService, AuthenticationServiceProvider }
import annette.core.security.authorization.AuthorizationActor
import annette.core.security.verification.VerificationBus
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provides }
import com.typesafe.config.Config
import javax.inject.{ Named, Singleton }
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

class AnnetteServerModule extends AbstractModule with ScalaModule {
  override def configure() = {
    bind(classOf[InitCoreTables]).asEagerSingleton()
    bind(classOf[CoreModule]).asEagerSingleton()
    bind[ActorRef].annotatedWith(Names.named(AuthenticationService.name)).toProvider(classOf[AuthenticationServiceProvider]).asEagerSingleton()

  }

  @Provides
  @Singleton
  @Named("CoreService")
  def getCoreService(actorSystem: ActorSystem, config: Config, verificationBus: VerificationBus)(implicit c: ExecutionContext, t: Timeout) = {
    actorSystem.actorOf(props = CoreService.props(
      config = config,
      verificationBus = verificationBus), name = CoreService.name)
  }

  @Provides
  @Singleton
  @Named("AuthorizationManager")
  def getAuthorizationManager(actorSystem: ActorSystem, config: Config)(implicit c: ExecutionContext, t: Timeout) = {
    actorSystem.actorOf(props = Props(new AuthorizationActor), name = "authorization")
  }
}
