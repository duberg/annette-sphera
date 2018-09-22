/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.dao

import javax.inject._

import annette.core.domain.DB
import annette.core.domain.tenancy.dao.tables._
import com.outworkers.phantom.dsl._
/**
 * Created by valery on 17.12.16.
 */

class TenancyDb @Inject() (val db: DB) extends Database[TenancyDb](db.keySpaceDef) {

  object tenants extends TenantTable with db.keySpaceDef.Connector

  object tenantUsers extends TenantUserTable with db.keySpaceDef.Connector

  object tenantUserRoles extends TenantUserRoleTable with db.keySpaceDef.Connector
  object userTenants extends UserTenantTable with db.keySpaceDef.Connector

  object users extends UserTable with db.keySpaceDef.Connector
  object emailPasswords extends UserEmailPasswordTable with db.keySpaceDef.Connector
  object phonePasswords extends UserPhonePasswordTable with db.keySpaceDef.Connector

  object openSessions extends OpenSessionTable with db.keySpaceDef.Connector
  object lastSessions extends LastSessionTable with db.keySpaceDef.Connector
  object sessionHistories extends SessionHistoryTable with db.keySpaceDef.Connector

}
