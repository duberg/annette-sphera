package annette.core.domain.tenancy.actor

import akka.actor.Props
import annette.core.akkaext.http.PageRequest
import annette.core.akkaext.http.Pagination._
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model._

class TenantServiceActor(val initState: TenantServiceState = TenantServiceState.empty) extends CqrsPersistentActor[TenantServiceState] {
  def createTenant(state: TenantServiceState, createTenant: CreateTenant): Unit = {
    val tenant = Tenant(
      id = createTenant.id,
      name = createTenant.name,
      defaultApplicationId = createTenant.defaultApplicationId,
      applications = createTenant.applications,
      defaultLanguageId = createTenant.defaultLanguageId,
      languages = createTenant.languages)
    persist(state, CreatedTenantEvt(tenant)) { (state, event) =>
      sender ! CreateTenantSuccess(tenant)
    }
  }

  def updateTenant(state: TenantServiceState, updateTenant: UpdateTenant): Unit = {
    ???
  }

  def deleteTenant(state: TenantServiceState, tenantId: Tenant.Id): Unit = {
    ???
  }

  def findTenantById(state: TenantServiceState, tenantId: Tenant.Id): Unit = {
    sender() ! TenantOpt(state.tenants.get(tenantId))
  }

  def listTenants(p1: TenantServiceState): Unit = {
    sender() ! TenantsMap(p1.tenants)
  }

  def paginateListTenants(state: TenantServiceState, page: PageRequest): Unit = {
    val (items, totalCount) = paginate(state.tenants, page)
    sender ! TenantsList(PaginateTenantsList(items, totalCount))
  }

  def behavior(state: TenantServiceState): Receive = {
    case CreateTenantCmd(x) => createTenant(state, x)
    case UpdateTenantCmd(x) => updateTenant(state, x)
    case DeleteTenantCmd(x) => deleteTenant(state, x)
    case GetTenantById(x) => findTenantById(state, x)
    case ListTenants => listTenants(state)
    case PaginateListTenants(x) => paginateListTenants(state, x)
  }
}

object TenantServiceActor {
  def props: Props = Props(new TenantServiceActor)
}
