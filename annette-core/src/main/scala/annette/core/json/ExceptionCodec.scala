package annette.core.json

import annette.core.exception.AnnetteException
import annette.core.http.ExceptionMapping
import io.circe._
import io.circe.syntax._

trait ExceptionCodec extends ExceptionMapping {
  implicit val encodeAnnetteException: Encoder[AnnetteException] = (exception: AnnetteException) => {
    val cause: Option[(String, Json)] = Option(exception.getCause).map { throwable =>
      "cause" -> toAnnetteException(throwable).asJson
    }

    val params: Seq[(String, Json)] =
      exception.exceptionMessage
        .map { case (k, v) => (k, v.asJson) }
        .toSeq

    val fields = cause
      .map(params :+ _)
      .getOrElse(params)

    Json.obj(fields: _*)
  }

}
