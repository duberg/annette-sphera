//package annette.core.domain.tenancy.dao
//
//import annette.core.domain.application.ApplicationManager
//import javax.inject._
//import annette.core.domain.application.Application
//import annette.core.domain.language.LanguageManager
//import annette.core.domain.language.model.Language
//import annette.core.domain.tenancy.{ UserManager, _ }
//import annette.core.domain.tenancy.model.{ Tenant, TenantUserRole, User }
//
//import scala.concurrent.{ ExecutionContext, Future }
//
///**
// * Created by valery on 18.12.16.
// */
//@Singleton
//class TenantUserRoleDao @Inject() (
//  db: TenancyDb,
//  userDao: UserManager) {
//
//  private def validateStore(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
//    for {
//      tenantExist <- db.tenants.isExist(tenantId)
//      userExist <- userDao.getById(userId).map(_.nonEmpty)
//    } yield {
//      if (!tenantExist) throw new TenantNotFound()
//      if (!userExist) throw new UserNotFound(userId)
//      (tenantId, userId)
//    }
//  }
//
//  private def storeInternal(roles: TenantUserRole)(implicit ec: ExecutionContext) = {
//    for {
//      tenantUserRoleRes <- db.tenantUserRoles.store(roles)
//    } yield (roles)
//  }
//
//  def store(roles: TenantUserRole)(implicit ec: ExecutionContext) = {
//    for {
//      validatedTenantUserRole <- validateStore(roles.tenantId, roles.userId)
//      createdTenantUserRole <- storeInternal(roles)
//    } yield createdTenantUserRole
//  }
//
//  def delete(tenantId: Tenant.Id, userId: User.Id)(implicit ec: ExecutionContext) = {
//    for {
//      _ <- db.tenantUserRoles.deleteById(tenantId, userId)
//    } yield ()
//  }
//
//  def getByIds(tenantId: Tenant.Id, userId: User.Id) = db.tenantUserRoles.getById(tenantId, userId)
//
//  def selectAll = db.tenantUserRoles.selectAll
//
//}
//
