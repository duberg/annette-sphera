package annette.core.domain.authorization.model

import annette.core.domain.tenancy.model.{ Tenant, User }

/**
 * Created by kantemirov on 04.02.17.
 */
case class UserPermission(
  tenantId: Tenant.Id,
  userId: User.Id,
  permissionId: Permission.Id,
  roleId: Role.Id,
  keys: Set[String])
