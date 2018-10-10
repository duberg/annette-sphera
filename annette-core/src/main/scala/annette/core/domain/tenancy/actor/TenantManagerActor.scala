package annette.core.domain.tenancy.actor

import java.util.UUID

import akka.actor.Props
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.http.{ Order, PageRequest }
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model.User.{ CreateUserSuccess, CreatedUserEvt, UsersList }

class TenantManagerActor(val id: ActorId, val initState: TenantManagerState) extends CqrsPersistentActor[TenantManagerState] {
  def createTenant(state: TenantManagerState, createTenant: CreateTenant): Unit = {
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

  def updateTenant(state: TenantManagerState, updateTenant: UpdateTenant): Unit = {
    ???
  }

  def deleteTenant(state: TenantManagerState, tenantId: Tenant.Id): Unit = {
    ???
  }

  def findTenantById(state: TenantManagerState, tenantId: Tenant.Id): Unit = {
    sender() ! TenantOpt(state.tenants.get(tenantId))
  }

  def listTenants(p1: TenantManagerState): Unit = {
    sender() ! TenantsMap(p1.tenants)
  }

  def paginateListTenants(state: TenantManagerState, page: PageRequest): Unit = {
    def sort = (state.tenants.values.toList /: page.sort) {
      case (users, (field, order)) =>

        field match {
          case "defaultLanguageId" =>
            order match {
              case Order.Asc => users.sortBy(_.defaultLanguageId)(Ordering.String)
              case Order.Desc => users.sortBy(_.defaultLanguageId)(Ordering.String.reverse)
            }
          case _ =>
            order match {
              case Order.Asc => users.sortBy(_.name)(Ordering.String)
              case Order.Desc => users.sortBy(_.name)(Ordering.String.reverse)
            }
        }
    }

    sender ! TenantsList(PaginateTenantsList(
      items = sort.slice(page.offset, page.offset + page.limit),
      totalCount = state.tenants.size))
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
