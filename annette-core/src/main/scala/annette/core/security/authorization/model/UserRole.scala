package annette.core.domain.authorization.model

import annette.core.domain.tenancy.model.{ Tenant, User }

/**
 * Created by kantemirov on 04.02.17.
 */
case class UserRole(
  tenantId: Tenant.Id,
  roleId: Role.Id,
  userId: User.Id)
