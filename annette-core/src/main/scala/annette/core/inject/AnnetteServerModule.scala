package annette.core.inject

import akka.actor.{ ActorRef, ActorSystem }
import annette.core.CoreModule
import annette.core.domain.{ CoreService, InitCoreTables }
import annette.core.security.authentication.{ AuthenticationService, AuthenticationServiceProvider }
import com.google.inject.name.Names
import com.google.inject.{ AbstractModule, Provides }
import javax.inject.{ Named, Singleton }
import net.codingwell.scalaguice.ScalaModule

class AnnetteServerModule extends AbstractModule with ScalaModule {
  override def configure() = {
    bind(classOf[InitCoreTables]).asEagerSingleton()
    bind(classOf[CoreModule]).asEagerSingleton()
    bind[ActorRef].annotatedWith(Names.named(AuthenticationService.name)).toProvider(classOf[AuthenticationServiceProvider]).asEagerSingleton()
  }

  @Provides
  @Singleton
  @Named("CoreService")
  def getCoreService(actorSystem: ActorSystem) = {
    actorSystem.actorOf(CoreService.props, "core")
  }
}
