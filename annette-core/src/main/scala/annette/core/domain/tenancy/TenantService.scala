package annette.core.domain.tenancy

import akka.actor.ActorRef
import akka.pattern.AskSupport
import akka.util.Timeout
import annette.core.akkaext.http.PageRequest
import annette.core.domain.application.{ Application, ApplicationService }
import annette.core.domain.language.LanguageService
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.Tenant._
import annette.core.domain.tenancy.model._
import javax.inject.{ Inject, Named, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class TenantService @Inject() (
  @Named("CoreService") actor: ActorRef,
  userManager: UserService,
  languageManager: LanguageService,
  applicationManager: ApplicationService)(implicit val c: ExecutionContext, val t: Timeout) extends AskSupport {

  def createTenant(x: CreateTenant): Future[Tenant] =
    ask(actor, CreateTenantCmd(x))
      .mapTo[Response]
      .map {
        case CreateTenantSuccess(x) => x
      }

  def getTenantById(x: Tenant.Id): Future[Option[Tenant]] =
    ask(actor, GetTenantById(x))
      .mapTo[TenantOpt]
      .map(_.x)

  def listTenants: Future[Map[Tenant.Id, Tenant]] =
    ask(actor, ListTenants)
      .mapTo[TenantsMap]
      .map(_.x)

  def paginateListTenants(page: PageRequest): Future[PaginateTenantsList] =
    ask(actor, PaginateListTenants(page))
      .mapTo[TenantsList]
      .map(_.x)

  def listTenantsIds: Future[Set[Tenant.Id]] =
    ask(actor, ListTenants)
      .mapTo[TenantsMap]
      .map(_.x.keys.toSet)

  def listTenantsIdsByUserId(x: User.Id): Future[Set[Tenant.Id]] =
    userManager
      .getUserById(x)
      .map(_.map(_.roles.keys.toSet)
        .getOrElse(Set.empty))

  def listTenantsByUserId(x: User.Id): Future[Map[Tenant.Id, Tenant]] =
    listTenantsIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(getTenantById))
        .map(_.flatten)
        .map(_.map(x => x.id -> x).toMap)
    }

  def listTenantsByUserIdSet(x: User.Id): Future[Set[Tenant]] =
    listTenantsIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(getTenantById))
        .map(_.flatten.toSet)
        .map(_.toSet)
    }

  def listApplicationsIdsByUserId(x: User.Id): Future[Set[Application.Id]] =
    listTenantsByUserId(x)
      .map(_.values.flatMap(_.applications).toSet)

  def listApplicationsByUserId(x: User.Id): Future[Map[Application.Id, Application]] =
    listApplicationsIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(applicationManager.getApplicationById))
        .map(_.flatten)
        .map(_.map(x => x.id -> x).toMap)
    }

  def listApplicationsByUserIdSet(x: User.Id): Future[Set[Application]] =
    listApplicationsIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(applicationManager.getApplicationById))
        .map(_.flatten)
        .map(_.toSet)
    }

  def listLanguagesIdsByUserId(x: User.Id): Future[Set[Language.Id]] =
    listTenants
      .map(_.flatMap(_._2.languages))
      .map(_.toSet)

  def listLanguagesByUserId(x: User.Id): Future[Map[Language.Id, Language]] =
    listLanguagesIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(languageManager.getLanguageById))
        .map(_.flatten)
        .map(_.map(x => x.id -> x).toMap)
    }

  def listLanguagesByUserIdSet(x: User.Id): Future[Set[Language]] =
    listLanguagesIdsByUserId(x).flatMap { y =>
      Future.sequence(y.map(languageManager.getLanguageById))
        .map(_.flatten)
        .map(_.toSet)
    }

  def isUserAssignedToTenant(tenantId: Tenant.Id, userId: User.Id): Future[Boolean] =
    listTenantsIdsByUserId(userId)
      .map(_ contains tenantId)

  //def paginateListTenants:

  def getUserTenantData(userId: User.Id): Future[Set[TenantData]] = {
    for {
      tenants <- listTenantsByUserIdSet(userId)
      applications <- listApplicationsByUserId(userId)
      languages <- listLanguagesByUserId(userId)
    } yield tenants map { tenant =>
      TenantData(
        name = tenant.name,
        apps = applications.values.toSet,
        lang = languages(tenant.defaultLanguageId),
        langs = languages.values.toSet,
        application = applications(tenant.defaultApplicationId),
        id = tenant.id)
    }
  }
}
