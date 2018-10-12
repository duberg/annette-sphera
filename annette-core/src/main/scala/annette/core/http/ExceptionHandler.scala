package annette.core.http

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes }
import akka.http.scaladsl.server.{ Directives, ExceptionHandler => AkkaExceptionHandler }
import annette.core.json._
import annette.core.{ AnnetteException, CoreException }

trait ExceptionHandler extends Directives {
  implicit def exceptionHandler(implicit log: LoggingAdapter): AkkaExceptionHandler = {
    AkkaExceptionHandler {
      case e: CoreException =>
        extractUri { uri =>
          complete(
            HttpResponse(
              entity = HttpEntity(
                ContentType(MediaTypes.`application/json`),
                e.asJson.pretty(printer)),
              status = InternalServerError))
        }
      case e: AnnetteException =>
        extractUri { uri =>
          complete(
            HttpResponse(
              entity = HttpEntity(
                ContentType(MediaTypes.`application/json`),
                e.asJson.pretty(printer)),
              status = InternalServerError))
        }
      case e: Throwable =>
        e.printStackTrace()

        complete {
          val annetteException = new AnnetteException(
            code = "bpm2.exceptions.UnknownException")
          HttpResponse(
            entity = HttpEntity(
              ContentType(MediaTypes.`application/json`),
              annetteException.asJson.pretty(printer)),
            status = InternalServerError)
        }
    }
  }
}