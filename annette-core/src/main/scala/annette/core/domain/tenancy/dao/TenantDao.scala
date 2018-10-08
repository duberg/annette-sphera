//package annette.core.domain.tenancy.dao
//
//import javax.inject._
//
//import annette.core.domain.tenancy.model.Tenant
//import annette.core.domain.tenancy.{ TenantAlreadyExists, _ }
//
//import scala.concurrent.{ ExecutionContext, Future }
//
//@Singleton
//class TenantDao @Inject() (db: TenancyDb) {
//  private def validateCreate(tenant: Tenant)(implicit ec: ExecutionContext): Future[Tenant] = {
//    for {
//      tenantExist <- db.tenants.isExist(tenant.id)
//    } yield {
//      // проверка существования пользователя
//      if (tenantExist) throw new TenantAlreadyExists()
//      tenant
//    }
//  }
//
//  private def createInternal(tenant: Tenant)(implicit ec: ExecutionContext): Future[Tenant] = {
//    for {
//      tenantRes <- db.tenants.store(tenant)
//    } yield tenant
//  }
//
//  def create(tenant: Tenant)(implicit ec: ExecutionContext): Future[Tenant] = {
//    for {
//      validatedTenant <- validateCreate(tenant)
//      createdTenant <- createInternal(tenant)
//    } yield createdTenant
//  }
//
//  private def validateUpdate(tenant: Tenant)(implicit ec: ExecutionContext) = {
//    for {
//      tenantOpt <- db.tenants.getById(tenant.id)
//    } yield {
//      // проверка существования пользователя
//      val oldTenant = tenantOpt.getOrElse(throw new TenantNotFound())
//      (oldTenant, tenant)
//    }
//  }
//
//  private def updateInternal(oldTenant: Tenant, newTenant: Tenant)(implicit ec: ExecutionContext) = {
//    for {
//      tenantRes <- db.tenants.store(newTenant)
//    } yield newTenant
//  }
//
//  def update(tenant: Tenant)(implicit ec: ExecutionContext): Future[Tenant] = {
//    for {
//      (oldTenant, validatedTenant) <- validateUpdate(tenant)
//      createdTenant <- updateInternal(oldTenant, validatedTenant)
//    } yield createdTenant
//  }
//
//  def getById(id: Tenant.Id): Future[Option[Tenant]] = db.tenants.getById(id)
//
//  def list(implicit ec: ExecutionContext): Future[Set[Tenant]] = db.tenants.selectAll.map(_.toSet)
//
//  def listIds(implicit ec: ExecutionContext): Future[Set[Tenant.Id]] = list.map(_.map(_.id))
//}
