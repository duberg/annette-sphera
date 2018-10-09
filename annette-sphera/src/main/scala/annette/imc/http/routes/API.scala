package annette.imc.http.routes

import java.io.File
import java.util.UUID

import annette.core.domain.tenancy.model.{ TenantUserRole, User }
import annette.imc.ApsActor
import annette.imc.model.Ap
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{ ImcUser, UserRoled }
import akka.pattern.ask

import scala.concurrent.Future

trait API { self: APIContext =>

  lazy val getUserRoled: UUID => Future[Option[UserRoled]] = ctx.getUserRoled

  def predicate(condition: Boolean)(fail: Exception): Future[Unit] =
    if (condition) Future(()) else Future.failed(fail)

  def getApById(apId: Ap.Id): Future[Ap] =
    ask(apsActor, ApsActor.GetApById(apId))
      .mapTo[ApsActor.ApFound]
      .map(_.ap)

  def getUsers(x: Set[User.Id]): Future[Set[User]] =
    Future.sequence(x.map(coreModule.userManager.getUserById))
      .map(_.flatten)

  def getImcUser(userId: User.Id): Future[Option[ImcUser]] =
    imcUserActor.ask(ImcUserActor.GetById(userId))
      .mapTo[SingleEntry]
      .map(_.maybeEntry)

  def getImcUsers(x: Set[User.Id]): Future[Set[ImcUser]] =
    Future.sequence(x.map(getImcUser))
      .map(_.flatten)

  def getAllImcUsers: Future[Map[UUID, ImcUser]] =
    imcUserActor.ask(ImcUserActor.GetAll).mapTo[ImcUserActor.MultipleEntries].map(_.entries)

  def getUsersAll: Future[Set[User]] = coreModule.userManager.listUsers.map(_.toSet)

  def getUserRoleAll: Future[Set[TenantUserRole]] = {
    Future.successful(Set.empty)
    //coreModule.tenantUserRoleDao.selectAll.map(_.toSet)
  }

  def applicant(ap: Ap, language: String): String = language match {
    case "EN" => ap.apData.entityName.map(_.en).getOrElse("")
    case _ => ap.apData.entityName.map(_.ru).getOrElse("")
  }
  def deleteFile(path: String): AnyVal = {
    val fileTemp = new File(path)
    if (fileTemp.exists) {
      fileTemp.delete()
    }
  }
}
