package annette.imc.report.retrieve

import java.time.LocalDate
import java.util.UUID
import java.time.format.DateTimeFormatter

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.domain.tenancy.model.{TenantUserRole, User}
import annette.imc.ApsActor
import annette.imc.model.Ap
import annette.imc.report.model.TemplateDataC
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{FullUser, ImcUser}

import scala.concurrent.{ExecutionContext, Future}

trait TemplateDataRetrieverD {
  def apsActor: ActorRef
  def imcUserActor: ActorRef
  implicit def t: Timeout
  implicit def c: ExecutionContext

  val coreModule: CoreModule

  private def getApById(apId: Ap.Id): Future[Ap] =
    ask(apsActor, ApsActor.GetApById(apId))
      .mapTo[ApsActor.ApFound]
      .map(_.ap)

  private def getUsers(x: Set[User.Id]): Future[Set[User]] =
    Future.sequence(x.map(coreModule.userManager.getUserById))
      .map(_.flatten)

  private def getImcUser(userId: User.Id): Future[Option[ImcUser]] =
    imcUserActor.ask(ImcUserActor.GetById(userId))
      .mapTo[SingleEntry]
      .map(_.maybeEntry)

  private def getImcUsers(x: Set[User.Id]): Future[Set[ImcUser]] =
    Future.sequence(x.map(getImcUser))
      .map(_.flatten)

  private def getAllImcUsers: Future[Map[UUID, ImcUser]] = {
    imcUserActor.ask(ImcUserActor.GetAll).mapTo[ImcUserActor.MultipleEntries].map(_.entries)
  }

  private def getUsersAll: Future[Set[User]] = coreModule.userManager.listUsers.map(_.toSet)

  private def getUserRoleAll: Future[Set[TenantUserRole]] = {
    //coreModule.tenantUserRoleDao.selectAll.map(_.toSet)
    ???
  }
  def retrieveDataD(apId: Ap.Id, expertId: User.Id, language: String): Future[TemplateDataC] = {
    if (language == "RU") retrieveRU(apId, expertId)
    else retrieveEN(apId, expertId)
  }

  private def retrieveEN(apId: Ap.Id, expertId: User.Id): Future[TemplateDataC] = {
    for {
      allUsers: Set[User] <- getUsersAll
      allUserRole: Set[TenantUserRole] <- getUserRoleAll
      allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
      ap: Ap <- getApById(apId)
    } yield {

      val x = for (user <- allUsers) yield {
        val roles = allUserRole.find(_.userId == user.id).map(_.roles)
        val imcUser = allImcUsers.get(user.id)

        FullUser(
          user.id,
          user.lastName,
          user.firstName,
          user.middleName.getOrElse(""),
          imcUser.flatMap(_.company),
          imcUser.flatMap(_.position),
          imcUser.flatMap(_.rank),
          roles.exists(_.contains("admin")),
          roles.exists(_.contains("secretar")),
          roles.exists(_.contains("manager")),
          roles.exists(_.contains("chairman")),
          roles.exists(_.contains("expert")),
          roles.exists(_.contains("additional")))
      }

      val refuseStr = "Refusal of voting due to personal interest in applicant or any other reason"

      val parameters = Map(
        "Applicant" -> ap.apData.entityName.map(_.ru).getOrElse(""),
        "Expert" -> x.toList.find(_.id == expertId).map(y => y.lastName + " " + y.firstName + " " + y.middleName).getOrElse(""),
        "Date" -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        "Reason" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.rejection match {
          case Some(true) =>
            y.negativeConclusion
              .map(s"$refuseStr: " + _)
              .orElse(Option(refuseStr))
          case _ => Some(refuseStr)
        }).getOrElse(refuseStr),
      )
      TemplateDataC(parameters)
    }
  }

  private def retrieveRU(apId: Ap.Id, expertId: User.Id): Future[TemplateDataC] = {
    for {
      allUsers: Set[User] <- getUsersAll
      allUserRole: Set[TenantUserRole] <- getUserRoleAll
      allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
      ap: Ap <- getApById(apId)
    } yield {

      val x = for (user <- allUsers) yield {
        val roles = allUserRole.find(_.userId == user.id).map(_.roles)
        val imcUser = allImcUsers.get(user.id)

        FullUser(
          user.id,
          user.lastName,
          user.firstName,
          user.middleName.getOrElse(""),
          imcUser.flatMap(_.company),
          imcUser.flatMap(_.position),
          imcUser.flatMap(_.rank),
          roles.exists(_.contains("admin")),
          roles.exists(_.contains("secretar")),
          roles.exists(_.contains("manager")),
          roles.exists(_.contains("chairman")),
          roles.exists(_.contains("expert")),
          roles.exists(_.contains("additional")))
      }

      val refuseStr = "Отказ от голосования"

      val parameters = Map(
        "Applicant" -> ap.apData.entityName.map(_.ru).getOrElse(""),
        "Expert" -> x.toList.find(_.id == expertId).map(y => y.lastName + " " + y.firstName + " " + y.middleName).getOrElse(""),
        "Date" -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        "Reason" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.rejection match {
          case Some(true) =>
            y.negativeConclusion
              .map(s"$refuseStr: " + _)
              .orElse(Option(refuseStr))
          case _ => Some(refuseStr)
        }).getOrElse(refuseStr),
        //        "Criterion1.ExpertOpinion" -> if(ap.expertise.bulletins),
      )
      TemplateDataC(parameters)
    }
  }
}
