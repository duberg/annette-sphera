package annette.core.domain.authorization.model

import annette.core.domain.tenancy.model.Tenant

/**
 * Created by valery on 04.02.17.
 */
case class Role(
  tenantId: Tenant.Id,
  roleId: Role.Id,
  description: String,
  activated: Boolean,
  permissionObjects: Map[Permission.Id, PermissionObject])

object Role {
  type Id = String
}
