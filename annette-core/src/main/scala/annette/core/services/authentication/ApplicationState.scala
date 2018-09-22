/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.services.authentication

import annette.core.domain.application.model.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.dao.TenantData
import annette.core.domain.tenancy.model.{ Tenant, User }

case class ApplicationState(
  authenticated: Boolean,
  language: Language,
  languages: Seq[Language],
  user: Option[User] = None,
  tenant: Option[Tenant] = None,
  application: Option[Application] = None,
  tenantData: Seq[TenantData] = Seq.empty,
  jwtToken: Option[String] = None)
