/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.dao

import javax.inject._

import annette.core.domain.DB
import annette.core.domain.authorization.dao.tables._
import com.outworkers.phantom.dsl._

/**
 * Created by valery on 17.12.16.
 */

class AuthorizationDb @Inject() (val db: DB) extends Database[AuthorizationDb](db.keySpaceDef) {

  object permissions extends PermissionTable with db.keySpaceDef.Connector
  object roles extends RoleTable with db.keySpaceDef.Connector
  object userRoles extends UserRoleTable with db.keySpaceDef.Connector
  object userPermissions extends UserPermissionTable with db.keySpaceDef.Connector
  object rolePermissions extends RolePermissionTable with db.keySpaceDef.Connector

  //roles.

}
