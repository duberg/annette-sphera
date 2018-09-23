package annette.imc.report.retrieve

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.domain.tenancy.model.{ TenantUserRole, User }
import annette.imc.ApsActor
import annette.imc.model.Ap
import annette.imc.report.model.TemplateDataC
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{ FullUser, ImcUser }

import scala.concurrent.{ ExecutionContext, Future }

trait TemplateDataRetrieverC {
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
    Future.sequence(x.map(coreModule.userDao.getById))
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

  private def getUsersAll: Future[Set[User]] = coreModule.userDao.selectAll.map(_.toSet)

  private def getUserRoleAll: Future[Set[TenantUserRole]] = coreModule.tenantUserRoleDao.selectAll.map(_.toSet)
  def retrieveDataC(apId: Ap.Id, expertId: User.Id, language: String): Future[TemplateDataC] = {
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

      val parameters = Map(
        "Applicant" -> ap.apData.entityName.map(_.en).getOrElse(""),
        "Expert" -> x.toList.find(_.id == expertId).map(y => y.lastName + " " + y.firstName + " " + y.middleName).getOrElse(""),
        "Date" -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        //        "Criterion1.ExpertOpinion" -> if(ap.expertise.bulletins),
        "Criterion1.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(1).map(_.decision) match {
          case Some(2) => "MEETS EXPECTATIONS"
          case Some(1) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "C1Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(1).map(_.pluses)).getOrElse(""),
        "C1Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(1).map(_.minuses)).getOrElse(""),
        "Criterion2.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(2).map(_.decision) match {
          case Some(2) => "MEETS EXPECTATIONS"
          case Some(1) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "C2Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(2).map(_.pluses)).getOrElse(""),
        "C2Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(2).map(_.minuses)).getOrElse(""),
        "Criterion3.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(3).map(_.decision) match {
          case Some(2) => "MEETS EXPECTATIONS"
          case Some(1) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "C3Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(3).map(_.pluses)).getOrElse(""),
        "C3Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(3).map(_.minuses)).getOrElse(""),

        "Criterion4.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(4).map(_.decision) match {
          case Some(2) => "MEETS EXPECTATIONS"
          case Some(1) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "C4Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(4).map(_.pluses)).getOrElse(""),
        "C4Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(4).map(_.minuses)).getOrElse(""),
        "Criterion5.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(5).map(_.decision) match {
          case Some(2) => "MEETS EXPECTATIONS"
          case Some(1) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "C5Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(5).map(_.pluses)).getOrElse(""),
        "C5Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(5).map(_.minuses)).getOrElse(""),
        "ExpertSummaryComments.StrengthsOfTheApplication" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.positiveConclusion).getOrElse(""),
        "ExpertSummaryComments.WeaknessesOfTheApplication" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.negativeConclusion).getOrElse(""),
        "Result" -> ap.expertise.bulletins.get(expertId).map(y => y.finalResult match {
          case Some(true) => "MEETS EXPECTATIONS"
          case Some(false) => "DOES NOT MEET EXPECTATIONS"
          case _ => ""
        }).getOrElse(""),
        "MedicalActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.medical)).getOrElse(0),
        "EducationActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.educational)).getOrElse(0),
        "ScientificActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.scientific)).getOrElse(0))
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

      val parameters = Map(
        "Applicant" -> ap.apData.entityName.map(_.ru).getOrElse(""),
        "Expert" -> x.toList.find(_.id == expertId).map(y => y.lastName + " " + y.firstName + " " + y.middleName).getOrElse(""),
        "Date" -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        //        "Criterion1.ExpertOpinion" -> if(ap.expertise.bulletins),
        "Criterion1.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(1).map(_.decision) match {
          case Some(2) => "СООТВЕТСТВУЕТ"
          case Some(1) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "C1Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(1).map(_.pluses)).getOrElse(""),
        "C1Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(1).map(_.minuses)).getOrElse(""),
        "Criterion2.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(2).map(_.decision) match {
          case Some(2) => "СООТВЕТСТВУЕТ"
          case Some(1) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "C2Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(2).map(_.pluses)).getOrElse(""),
        "C2Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(2).map(_.minuses)).getOrElse(""),
        "Criterion3.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(3).map(_.decision) match {
          case Some(2) => "СООТВЕТСТВУЕТ"
          case Some(1) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "C3Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(3).map(_.pluses)).getOrElse(""),
        "C3Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(3).map(_.minuses)).getOrElse(""),

        "Criterion4.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(4).map(_.decision) match {
          case Some(2) => "СООТВЕТСТВУЕТ"
          case Some(1) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "C4Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(4).map(_.pluses)).getOrElse(""),
        "C4Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(4).map(_.minuses)).getOrElse(""),
        "Criterion5.ExpertOpinion" -> ap.expertise.bulletins.get(expertId).map(y => y.criterions.get(5).map(_.decision) match {
          case Some(2) => "СООТВЕТСТВУЕТ"
          case Some(1) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "C5Pluses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(5).map(_.pluses)).getOrElse(""),
        "C5Minuses" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.criterions.get(5).map(_.minuses)).getOrElse(""),
        "ExpertSummaryComments.StrengthsOfTheApplication" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.positiveConclusion).getOrElse(""),
        "ExpertSummaryComments.WeaknessesOfTheApplication" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.negativeConclusion).getOrElse(""),
        "Result" -> ap.expertise.bulletins.get(expertId).map(y => y.finalResult match {
          case Some(true) => "СООТВЕТСТВУЕТ"
          case Some(false) => "НЕ СООТВЕТСТВУЕТ"
          case _ => ""
        }).getOrElse(""),
        "MedicalActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.medical)).getOrElse(0),
        "EducationActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.educational)).getOrElse(0),
        "ScientificActivityScore" -> ap.expertise.bulletins.get(expertId).flatMap(y => y.scores.map(_.scientific)).getOrElse(0))
      TemplateDataC(parameters)
    }
  }
}
