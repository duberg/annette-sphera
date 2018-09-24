package annette.core.json

import annette.core.exception.AnnetteException

case class JsonError(
  httpErrorStatusCode: Int,
  httpErrorMessage: String,
  httpReason: String,
  error: AnnetteException)