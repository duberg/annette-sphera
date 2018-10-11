package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.model.Tenant
import annette.core.domain.tenancy.model.Tenant._

case class TenantServiceState(tenants: Map[Tenant.Id, Tenant]) extends CqrsState {
  def createTenant(x: Tenant): TenantServiceState = {
    copy(tenants = tenants + (x.id -> x))
  }

  def update: Update = {
    case CreatedTenantEvt(x) => createTenant(x)
  }
}

object TenantServiceState {
  def empty: TenantServiceState = TenantServiceState(Map.empty)
}