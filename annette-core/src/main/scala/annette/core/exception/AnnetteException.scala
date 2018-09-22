package annette.core.exception

class AnnetteException(code: String, params: Map[String, String] = Map.empty, cause: Option[Throwable] = None) extends RuntimeException(code, cause.orNull) {
  def exceptionMessage: Map[String, String] = params + ("code" -> code)
}

class AnnetteMessageException(val message: AnnetteMessage) extends AnnetteException(message.code, message.params)