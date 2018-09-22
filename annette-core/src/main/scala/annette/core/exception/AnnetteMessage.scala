package annette.core.exception

abstract class AnnetteMessage(val code: String, val params: Map[String, String] = Map.empty) {
  def toException: AnnetteMessageException
}