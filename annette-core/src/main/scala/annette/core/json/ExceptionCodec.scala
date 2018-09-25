package annette.core.json

import annette.core.{ AnnetteException, CoreException }
import annette.core.http.ExceptionMapping
import io.circe._

trait ExceptionCodec extends ExceptionMapping {
  implicit val encodeAnnetteException: Encoder[AnnetteException] = (exception: AnnetteException) => {
    Json.obj(
      "code" -> exception.code.asJson,
      "parameters" -> exception.parameters.asJson)
  }
  implicit val encodeCoreException: Encoder[CoreException] = (exception: CoreException) => {
    Json.obj(
      "code" -> exception.code.asJson,
      "parameters" -> exception.parameters.asJson)
  }
}
