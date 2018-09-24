package annette.core.json

import annette.core.exception.AnnetteException

case class JsonErrors(
  httpErrorStatusCode: Int,
  httpErrorMessage: String,
  httpReason: String,
  errors: Seq[AnnetteException])