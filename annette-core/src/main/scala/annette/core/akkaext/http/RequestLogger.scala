package annette.core.akkaext.http

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.directives.{ DebuggingDirectives, LoggingMagnet }

//trait RequestLogger extends DebuggingDirectives with Loggab {
//
//  def logAllRequests: Directive0 = DebuggingDirectives.logRequest(LoggingMagnet(_ => logRequest))
//
//  private def logRequest(request: HttpRequest): Unit = {
//    logger.info(s"[${request.method.value}] '${request.uri.toRelative} ${request.protocol.value}'}")
//  }
//
//}
