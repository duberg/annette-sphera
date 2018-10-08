package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.model.Tenant

case class TenantManagerState(tenants: Map[Tenant.Id, Tenant]) extends CqrsState {


  def update: Update = {

  }
}
