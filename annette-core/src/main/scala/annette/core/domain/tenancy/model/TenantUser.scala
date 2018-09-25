package annette.core.domain.tenancy.model

/**
 * Created by valery on 17.12.16.
 */
case class TenantUser(
  tenantId: Tenant.Id,
  userId: User.Id)
