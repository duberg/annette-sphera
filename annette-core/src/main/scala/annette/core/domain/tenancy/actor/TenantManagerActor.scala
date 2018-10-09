package annette.core.domain.tenancy.actor

import java.util.UUID

import akka.actor.Props
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.http.PageRequest
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.model.{ CreateTenant, Tenant, UpdateTenant }
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model.User.{ CreateUserSuccess, CreatedUserEvt }

class TenantManagerActor(val id: ActorId, val initState: TenantManagerState) extends CqrsPersistentActor[TenantManagerState] {
  def createTenant(p1: TenantManagerState, p2: CreateTenant): Unit = {
    val tenant = Tenant(
      id = p2.id,
      name = p2.name,
      defaultApplicationId = p2.defaultApplicationId,
      applications = p2.applications,
      defaultLanguageId = p2.defaultLanguageId,
      languages = p2.languages)
    persist(p1, CreatedTenantEvt(tenant)) { (state, event) =>
      sender ! CreateTenantSuccess(tenant)
    }
  }

  def updateTenant(p1: TenantManagerState, p2: UpdateTenant): Unit = {
    ???
  }

  def deleteTenant(p1: TenantManagerState, p2: Tenant.Id): Unit = {
    ???
  }

  def findTenantById(p1: TenantManagerState, p2: Tenant.Id): Unit = {
    sender() ! TenantOpt(p1.tenants.get(p2))
  }

  def listTenants(p1: TenantManagerState): Unit = {
    sender() ! TenantsMap(p1.tenants)
  }

  def paginateListTenants(p1: TenantManagerState, p2: PageRequest): Unit = {

  }

  def behavior(state: TenantManagerState): Receive = {
    case CreateTenantCmd(x) => createTenant(state, x)
    case UpdateTenantCmd(x) => updateTenant(state, x)
    case DeleteTenantCmd(x) => deleteTenant(state, x)
    case GetTenantById(x) => findTenantById(state, x)
    case ListTenants => listTenants(state)
    case PaginateListTenants(x) => paginateListTenants(state, x)
  }
}

object TenantManagerActor {
  def props(id: ActorId, initState: TenantManagerState = TenantManagerState.empty): Props =
    Props(new TenantManagerActor(id, initState))
}
