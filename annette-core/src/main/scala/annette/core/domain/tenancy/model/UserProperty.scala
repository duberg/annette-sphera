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
package annette.core.domain.tenancy.model

import annette.core.domain.application.model.Application

case class UserProperty(
  id: UserProperty.Id,
  value: String)

object UserProperty {

  case class Id(
    userId: User.Id,
    tenantId: Option[Tenant.Id] = None,
    applicationId: Option[Application.Id] = None,
    key: String)

}