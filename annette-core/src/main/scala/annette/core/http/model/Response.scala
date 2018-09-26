package annette.core.http.model

case class Response(entityId: String, status: ResponseStatus)

sealed trait ResponseStatus

object ResponseStatus {
  case object Pending extends ResponseStatus
  case object Running extends ResponseStatus
  case object Done extends ResponseStatus
}