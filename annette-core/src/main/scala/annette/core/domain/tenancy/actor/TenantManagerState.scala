package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.model.Tenant
import annette.core.domain.tenancy.model.Tenant._

case class TenantManagerState(tenants: Map[Tenant.Id, Tenant]) extends CqrsState {
  def createTenant(x: Tenant): TenantManagerState = {
    copy(tenants = tenants + (x.id -> x))
  }

  def update: Update = {
    case CreatedTenantEvt(x) => createTenant(x)
  }
}

object TenantManagerState {
  def empty: TenantManagerState = TenantManagerState(Map.empty)
}