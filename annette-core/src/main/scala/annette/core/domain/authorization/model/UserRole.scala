/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.model

import annette.core.domain.tenancy.model.{ Tenant, User }

/**
 * Created by valery on 04.02.17.
 */
case class UserRole(
  tenantId: Tenant.Id,
  roleId: Role.Id,
  userId: User.Id)