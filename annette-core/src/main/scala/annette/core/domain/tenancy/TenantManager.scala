package annette.core.domain.tenancy

import akka.actor.ActorRef
import akka.pattern.AskSupport
import akka.util.Timeout
import annette.core.domain.tenancy.model.{ CreateTenant, Tenant }
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import annette.core.domain.tenancy.model.Tenant._

@Singleton
class TenantManager @Inject() (@Named("CoreService") actor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) extends AskSupport {
  def createTenant(x: CreateTenant): Future[Tenant] =
    ask(actor, CreateTenantCmd(x))
      .mapTo[Response]
      .map {
        case CreateTenantSuccess(x) => x
      }

  def getById(x: Tenant.Id): Future[Option[Tenant]] =
    ask(actor, GetTenantById(x))
      .mapTo[TenantOpt]
      .map(_.x)

  def listTenants: Future[Map[Tenant.Id, Tenant]] =
    ask(actor, ListTenants)
      .mapTo[TenantsMap]
      .map(_.x)

  def listTenantsIds: Future[Set[Tenant.Id]] =
    ask(actor, ListTenants)
      .mapTo[TenantsMap]
      .map(_.x.keys.toSet)

  //def paginateListTenants:
}
