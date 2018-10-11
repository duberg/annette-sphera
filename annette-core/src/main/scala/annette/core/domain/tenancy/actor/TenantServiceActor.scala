package annette.core.domain.tenancy.actor

import java.util.UUID

import akka.actor.Props
import annette.core.akkaext.actor.ActorId
import annette.core.akkaext.http.{ Order, PageRequest }
import annette.core.akkaext.persistence.CqrsPersistentActor
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model.User.{ CreateUserSuccess, CreatedUserEvt, UsersList }
import shapeless._
import shapeless.MkFieldLens._
import shapeless.ops.record.Keys
import shapeless.nat._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe

class TenantServiceActor(val id: ActorId, val initState: TenantServiceState) extends CqrsPersistentActor[TenantServiceState] {
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
    println(state)
    sender() ! TenantOpt(state.tenants.get(tenantId))
  }

  def listTenants(p1: TenantServiceState): Unit = {
    sender() ! TenantsMap(p1.tenants)
  }

  def paginateListTenants(state: TenantServiceState, page: PageRequest): Unit = {
    import scala.reflect.ClassTag
    import scala.reflect.runtime.universe
    import scala.reflect.runtime.universe._

    def getObjField[T: TypeTag: ClassTag](field: String, obj: T): Any = {
      val rm = scala.reflect.runtime.currentMirror
      val accessors: Iterable[universe.MethodSymbol] = typeOf[T].members.collect {
        case m: MethodSymbol if m.isGetter && m.isPublic => m
      }
      val fieldSymbol = accessors.find(_.name.toString == field).get
      val instanceMirror = rm.reflect(obj)
      instanceMirror.reflectMethod(fieldSymbol).apply()
    }

    implicit object AnyOrdering extends Ordering[Any] {
      def compare(x: Any, y: Any): Int = (x, y) match {
        case (a: Int, b: Int) => Ordering.Int.compare(a, b)
        case (a: Boolean, b: Boolean) => Ordering.Boolean.compare(a, b)
        case (a: BigDecimal, b: BigDecimal) => Ordering.BigDecimal.compare(a, b)
        case (a, b) => Ordering.String.compare(a.toString, b.toString)
      }
    }

    def sort = (state.tenants.values.toList /: page.sort) {
      case (users, (field, order)) =>

        val f = if (field == "") "id" else field

        order match {
          case Order.Asc => users.sortBy(getObjField(f, _))(AnyOrdering)
          case Order.Desc => users.sortBy(getObjField(f, _))(AnyOrdering.reverse)
        }

      //        field match {
      //          case "defaultLanguageId" =>
      //
      //          case _ =>
      //            order match {
      //              case Order.Asc => users.sortBy(_.name)(Ordering.String)
      //              case Order.Desc => users.sortBy(_.name)(Ordering.String.reverse)
      //            }
      //        }
    }

    sender ! TenantsList(PaginateTenantsList(
      items = sort.slice(page.offset, page.offset + page.limit),
      totalCount = state.tenants.size))
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
  def props(id: ActorId, initState: TenantServiceState = TenantServiceState.empty): Props =
    Props(new TenantServiceActor(id, initState))
}
