package annette.core

import akka.actor.ActorSystem
import akka.event.{ LogSource, Logging }
import annette.core.http.AnnetteHttpServer
import annette.core.inject.{ AkkaModule, AnnetteServerModule }
import com.google.inject.Guice
import com.typesafe.config.{ Config, ConfigException }
import net.codingwell.scalaguice.InjectorExtensions._

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.Try

class AnnetteServer {
  System.setProperty("logback.configurationFile", "conf/logback.xml")

  val injector = Guice.createInjector(
    new AkkaModule(),
    new AnnetteServerModule())

  val system = injector.instance[ActorSystem]
  val config = injector.instance[Config]
  val coreModule = injector.instance[CoreModule]

  implicit val ec = system.dispatcher

  implicit val myLogSourceType: LogSource[AnnetteServer] = (a: AnnetteServer) => "AnnetteServer"

  val log = Logging(system, this)

  private def getObjectByName[T](clazz: String)(implicit man: Manifest[T]) = {
    val module = Class.forName(clazz).newInstance().asInstanceOf[AnnetteModule]
    module.coreModule = coreModule
    module
  }

  private def getModules: List[AnnetteModule] = {
    Try {
      config.getStringList("annette.enabled").asScala.toList.flatMap {
        moduleName =>
          val module = getObjectByName[AnnetteModule](moduleName)
          if (module.isInstanceOf[AnnetteModule]) Some(module.asInstanceOf[AnnetteModule])
          else {
            log.error("Module {} is not AnnetteModule", moduleName)
            None
          }
      }
    }.recover {
      case th: ConfigException.Missing =>
        log.error("Missing configuration key 'annette.enabled'")
        List.empty
      case th: ConfigException.WrongType =>
        log.error("Configuation key 'annette.enabled' has wrong type")
        List.empty
      case th: Throwable =>
        log.error(th, "Error processing configuration key 'annette.enabled'")
        List.empty
    }.toOption.getOrElse(List.empty)

  }

  def run() = {

    val httpActive = config.getBoolean("annette.http.active")

    val modules: List[AnnetteModule] = getModules

    (coreModule :: modules).foreach {
      module =>
        log.info("Module enabled: {} ({})", module.name, module.buildInfo)
    }

    log.info("Modules initializing...")
    val initModules = coreModule :: modules
    val initFuture = Future.sequence(initModules.map(_.init()))
    Await.ready(initFuture, 10.minutes)
    log.info("Modules initialized")

    val httpModules = modules.flatMap {
      case m if m.isInstanceOf[AnnetteHttpModule] =>
        Some(m.asInstanceOf[AnnetteHttpModule])
      case _ =>
        None
    }

    if (httpActive) {
      val _http = new AnnetteHttpServer(coreModule)
      _http.run(httpModules)
    }

    log.info("Annette Server started")

    Await.result(system.whenTerminated, Duration.Inf)
  }

}
