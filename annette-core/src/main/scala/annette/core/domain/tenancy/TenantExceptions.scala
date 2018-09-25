package annette.core.domain.tenancy

import annette.core.{ AnnetteMessage, AnnetteMessageException }

case class TenantAlreadyExistsMsg() extends AnnetteMessage("core.tenancy.tenant.alreadyExists") {
  override def toException = new TenantAlreadyExists
}
class TenantAlreadyExists extends AnnetteMessageException(TenantAlreadyExistsMsg())

case class TenantNotFoundMsg() extends AnnetteMessage("core.tenancy.tenant.notFound") {
  override def toException = new TenantNotFound
}
class TenantNotFound extends AnnetteMessageException(TenantNotFoundMsg())