package annette.core.http

import akka.Done
import akka.event.{ LogSource, Logging }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import annette.core.{ AnnetteHttpModule, CoreModule }

import scala.concurrent.Future

class AnnetteHttpServer(coreModule: CoreModule) {

  implicit val system = coreModule.system
  implicit val materializer = coreModule.materializer
  implicit val executionContext = system.dispatcher

  implicit val myLogSourceType: LogSource[AnnetteHttpServer] = new LogSource[AnnetteHttpServer] {
    def genString(a: AnnetteHttpServer) = "AnnetteHttpServer"
  }

  val log = Logging(system, this)

  var bindingFuture: Option[Future[Http.ServerBinding]] = None

  def run(modules: List[AnnetteHttpModule]): Unit = {
    val host = coreModule.config.getString("annette.http.host")
    val port = coreModule.config.getInt("annette.http.port")

    bindingFuture = Some(Http().bindAndHandle(routes(modules), host, port))
    log.info(s"Listen to http://$host:$port")
  }

  def stop(): Option[Future[Done]] = {
    bindingFuture.map(_.flatMap(_.unbind()))
  }

  private def routes(modules: List[AnnetteHttpModule]): Route = {
    var routes: Route = {
      pathEndOrSingleSlash {
        getFromResource("dist/index.html")
      } ~
        path(Segment) { file =>
          getFromResource(s"dist/$file")
        } ~
        pathPrefix("dist") {
          getFromResourceDirectory("dist/")
        } ~
        pathPrefix("assets") {
          getFromResourceDirectory("dist/assets/")
        } ~
        (pathPrefix(!"api") & get) {
          getFromResource("dist/index.html")
        } ~
        coreModule.routes
    }

    modules.foreach { module =>
      log.info(s"Routes initialized for module ${module.name}")
      routes = routes ~ module.routes
    }

    routes
  }

}