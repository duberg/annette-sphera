package annette.core.domain.tenancy.model

case class TenantUserRole(
  tenantId: Tenant.Id,
  userId: User.Id,
  roles: Set[String])

