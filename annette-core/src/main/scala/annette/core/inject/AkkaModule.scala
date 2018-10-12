package annette.core.inject

import akka.actor.ActorSystem
import akka.util.Timeout
import annette.core.inject.AkkaModule.{ ActorSystemProvider, ExecutionContextProvider, TimeoutProvider }
import com.google.inject.{ AbstractModule, Injector, Provider }
import com.typesafe.config.Config
import javax.inject.Inject
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object AkkaModule {
  class ActorSystemProvider @Inject() (val config: Config, val injector: Injector) extends Provider[ActorSystem] {
    override def get() = {
      val system = ActorSystem("annette", config)
      // add the GuiceAkkaExtension to the system, and initialize it with the Guice injector
      GuiceAkkaExtension(system).initialize(injector)
      system
    }
  }

  class ExecutionContextProvider @Inject() (val system: ActorSystem, val injector: Injector) extends Provider[ExecutionContext] {
    override def get() = system.dispatcher
  }

  class TimeoutProvider @Inject() (val system: ActorSystem, val injector: Injector) extends Provider[Timeout] {
    override def get() = 10.seconds
  }
}

/**
 * A module providing an Akka ActorSystem.
 */
class AkkaModule extends AbstractModule with ScalaModule {
  def configure() {
    bind[Config].toProvider[ConfigProvider].asEagerSingleton()
    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()
    bind[ExecutionContext].toProvider[ExecutionContextProvider].asEagerSingleton()
    bind[Timeout].toProvider[TimeoutProvider].asEagerSingleton()
  }
}
