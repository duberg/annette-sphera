/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */
package annette.core.domain.tenancy

import annette.core.exception.{ AnnetteMessage, AnnetteMessageException }

case class TenantAlreadyExistsMsg() extends AnnetteMessage("core.tenancy.tenant.alreadyExists") {
  override def toException = new TenantAlreadyExists
}
class TenantAlreadyExists extends AnnetteMessageException(TenantAlreadyExistsMsg())

case class TenantNotFoundMsg() extends AnnetteMessage("core.tenancy.tenant.notFound") {
  override def toException = new TenantNotFound
}
class TenantNotFound extends AnnetteMessageException(TenantNotFoundMsg())
