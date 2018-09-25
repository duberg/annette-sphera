package annette.core

abstract class AnnetteMessage(val code: String, val params: Map[String, String] = Map.empty) {
  def toException: AnnetteMessageException
}