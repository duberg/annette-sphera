package annette.core.domain.tenancy

import akka.actor.ActorRef
import akka.pattern.AskSupport
import akka.util.Timeout
import annette.core.domain.application.{Application, ApplicationManager}
import annette.core.domain.language.LanguageManager
import annette.core.domain.tenancy.dao.TenantData
import annette.core.domain.tenancy.model.{CreateTenant, Tenant, User}
import javax.inject.{Inject, Named, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import annette.core.domain.tenancy.model.Tenant._

@Singleton
class TenantManager @Inject() (
                                @Named("CoreService") actor: ActorRef,
                                userManager: UserManager,
                                tenantManager: TenantManager,
                                languageManager: LanguageManager,
                                applicationManager: ApplicationManager)(implicit val c: ExecutionContext, val t: Timeout) extends AskSupport {

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

  def listTenantsIds: Future[Set[Tenant.Id]] =
    ask(actor, ListTenants)
      .mapTo[TenantsMap]
      .map(_.x.keys.toSet)

  def listApplications(x: Tenant.Id): Future[Set[Application]] =
    getTenantById(x).map(_.map(_.applications))

  //def paginateListTenants:

  def getUserTenantData(userId: User.Id): Future[Seq[TenantData]] = {
    val tenantsFuture = userManager.listTenantsIds(userId)
    val applicationsFuture = tenantsFuture.flatMap {
      tenants =>
        val applicationIds = tenants.flatMap(_.applications)
        Future.sequence(applicationIds.map(a => applicationManager.getById(a))).map(_.flatten)
    }
    val languagesFuture = tenantsFuture.flatMap {
      tenants =>
        val languageIds = tenants.flatMap(_.languages)
        Future.sequence(languageIds.map(a => languageManager.getById(a))).map(_.flatten)
    }

    for {
      tenants <- tenantsFuture.map(_.toSeq)
      applications <- applicationsFuture.map(_.toSeq)
      languages <- languagesFuture.map(_.toSeq)
    } yield {
      println("getUserTenantData")
      println(tenants)
      println(applications)
      println(languages)
      val appMap = applications.map(a => a.id -> a).toMap
      val langMap = languages.map(a => a.id -> a).toMap
      tenants.map {
        tenant =>
          val apps = tenant.applications
            .map(a => appMap.get(a))
            .flatten.toSeq
          val langs = tenant.languages
            .map(a => langMap.get(a))
            .flatten.toSeq
          val lang = langMap.get(tenant.defaultLanguageId).get
          TenantData(tenant.name, apps, lang, langs, appMap.get(tenant.defaultApplicationId).get, tenant.id)
      }
    }

  }
}
