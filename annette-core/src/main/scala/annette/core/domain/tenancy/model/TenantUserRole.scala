package annette.core.domain.tenancy.model

/**
 * Created by valery on 17.12.16.
 */
case class TenantUserRole(
  tenantId: Tenant.Id,
  userId: User.Id,
  roles: Set[String])

