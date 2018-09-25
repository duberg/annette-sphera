package annette.core

class AnnetteException(val code: String, val parameters: Map[String, String] = Map.empty, cause: Option[Throwable] = None) extends RuntimeException(code, cause.orNull) {
  def exceptionMessage: Map[String, String] = parameters + ("code" -> code)
}

class AnnetteMessageException(val message: AnnetteMessage) extends AnnetteException(message.code, message.params)