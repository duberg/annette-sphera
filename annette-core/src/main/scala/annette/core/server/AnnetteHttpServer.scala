/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */
package annette.core.server

import akka.event.{ LogSource, Logging }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import annette.core.CoreModule
import annette.core.modularize.AnnetteHttpModule

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

  def stop(): Option[Future[Unit]] = {
    bindingFuture.map(_.flatMap(_.unbind()))
  }

  private def routes(modules: List[AnnetteHttpModule]): Route = {
    var routes: Route = pathEndOrSingleSlash {
      getFromResource("dist/index.html")
      //complete("ok")
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
      coreModule.routes

    modules.foreach { module =>
      log.info(s"Routes initialized for module ${module.name}")
      routes = routes ~ module.routes
    }

    /* routes = routes ~ pathPrefix("") {
      getFromResource("dist/index.html")
    }*/

    routes
    //    ~
    //      (path(Segments) & get) { any =>
    //        getFromResource("dist/index.html")
    //      }

  }

}