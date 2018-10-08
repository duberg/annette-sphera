package annette.core.domain.tenancy.actor

import java.util.UUID

import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.http.PageRequest
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.model.{CreateTenant, UpdateTenant}
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model.Tenant
import annette.core.domain.tenancy.model.User.{CreateUserSuccess, CreatedUserEvt}

class TenantManagerActor(val id: ActorId, val initState: TenantManagerState) extends CqrsPersistentActor[TenantManagerState] {
  def createTenant(p1: TenantManagerState, p2: CreateTenant): Unit = {
    val tenant = Tenant(
      id = p2.id,
      name = p2.name,
      defaultApplicationId = p2.defaultApplicationId,
      applications = p2.applications,
      defaultLanguageId = p2.defaultLanguageId,
      languages = p2.languages
    )
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

  }

  def listTenants(p1: TenantManagerState): Unit = {

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
