package annette.core.domain.tenancy

import annette.core.{ AnnetteMessage, AnnetteMessageException }

case class SessionAlreadyExistsMsg() extends AnnetteMessage("core.tenancy.session.alreadyExists") {
  override def toException = new SessionAlreadyExists
}
class SessionAlreadyExists extends AnnetteMessageException(SessionAlreadyExistsMsg())

case class SessionNotFoundMsg() extends AnnetteMessage("core.tenancy.session.notFound") {
  override def toException = new SessionNotFound
}
class SessionNotFound extends AnnetteMessageException(SessionNotFoundMsg())
