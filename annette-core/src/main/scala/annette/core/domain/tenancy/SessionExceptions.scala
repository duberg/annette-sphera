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

case class SessionAlreadyExistsMsg() extends AnnetteMessage("core.tenancy.session.alreadyExists") {
  override def toException = new SessionAlreadyExists
}
class SessionAlreadyExists extends AnnetteMessageException(SessionAlreadyExistsMsg())

case class SessionNotFoundMsg() extends AnnetteMessage("core.tenancy.session.notFound") {
  override def toException = new SessionNotFound
}
class SessionNotFound extends AnnetteMessageException(SessionNotFoundMsg())
