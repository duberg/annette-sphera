//package annette.core.domain.authorization.service
//
//import javax.inject.Inject
//
//import akka.Done
//import annette.core.domain.authorization.RoleNotFound
//import annette.core.domain.authorization.dao.{ RoleDao, UserPermissionDao, UserRoleDao }
//import annette.core.domain.authorization.model.{ Role, UserPermission, UserRole }
//import annette.core.domain.tenancy.model.Tenant.Id
//import annette.core.domain.tenancy.model.User
//
//import scala.concurrent.{ ExecutionContext, Future }
//
//class RoleActivationImpl @Inject() (
//  roleDao: RoleDao,
//  userRoleDao: UserRoleDao,
//  userPermissionDao: UserPermissionDao) extends RoleActivation {
//
//  def processActivation(roleOpt: Option[Role], userRoles: List[UserRole],
//    userPermissions: List[UserPermission])(implicit ec: ExecutionContext) = {
//    roleOpt.map {
//      role =>
//        // формируем множество ключей userPermission
//        var userPermissionsToDeleteSet = userPermissions
//          .map { p => (p.tenantId, p.userId, p.permissionId, p.roleId) }
//          .toSet
//        // формируем список userPermission которые надо создать
//        val userPermissionsToCreate = (for {
//          permissionObject <- role.permissionObjects.values
//          userId <- userRoles.map(_.userId)
//        } yield UserPermission(role.tenantId, userId, role.roleId,
//          permissionObject.permissionId, permissionObject.keys))
//          .toList
//        // формируем множество ключей userPermission которые надо создать
//        val userPermissionsToCreateSet = userPermissionsToCreate
//          .map(p => (p.tenantId, p.userId, p.permissionId, p.roleId)).toSet
//
//        // исключаем из множества всех ключей множество ключей которые надо создать
//        userPermissionsToDeleteSet --= userPermissionsToCreateSet
//
//        // удаляем userPermissions
//        val deleteFuture = Future
//          .traverse(userPermissionsToDeleteSet) { p => userPermissionDao.deleteUser(p._1, p._2, p._3, p._4) }
//          .map(p => Done)
//        // создаём userPermissions
//        val createFuture = Future
//          .traverse(userPermissionsToCreate)(userPermissionDao.store)
//          .map(p => Done)
//
//        Future
//          .sequence(List(deleteFuture, createFuture))
//          .map(p => Done)
//    }.getOrElse(Future.failed(new RoleNotFound()))
//  }
//
//  override def activate(tenantId: Id, roleId: Id)(implicit ec: ExecutionContext): Future[Done] = {
//    for {
//      roleOpt <- roleDao.getById(tenantId, roleId)
//      userRoles <- userRoleDao.selectByRole(tenantId, roleId)
//      userPermissions <- userPermissionDao.selectByTenantAndRoleId(tenantId, roleId)
//      _ <- processActivation(roleOpt, userRoles, userPermissions)
//    } yield Done
//  }
//
//  override def updateRolePermissionsForUser(tenantId: Id, roleId: Id, userId: User.Id)(implicit ec: ExecutionContext): Future[Done] = ???
//}
