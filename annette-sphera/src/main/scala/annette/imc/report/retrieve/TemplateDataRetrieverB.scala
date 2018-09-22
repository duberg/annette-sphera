package annette.imc.report.retrieve

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.domain.tenancy.model.{ TenantUserRole, User }
import annette.imc.ApsActor
import annette.imc.model.Ap
import annette.imc.report.model.TemplateDataB
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{ FullUser, ImcUser }

import scala.concurrent.{ ExecutionContext, Future }

trait TemplateDataRetrieverB {
  implicit def c: ExecutionContext
  implicit def t: Timeout

  def apsActor: ActorRef
  def imcUserActor: ActorRef

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

  def retrieveDataB(apId: Ap.Id, language: String): Future[TemplateDataB] = {
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
          user.lastname,
          user.firstname,
          user.middlename,
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

      val chairman = x.find(_.chairman)
      val chairmanName = chairman.map(y => s"${y.lastname} ${y.firstname.take(1)}.${
        y.middlename match {
          case x: String if x == "" => ""
          case x: String => " " + x.take(1) + "."
        }
      }").getOrElse("")
      val secretar = x.find(_.secretar)
      val secretarName = secretar.map(y => s"${y.lastname} ${y.firstname.take(1)}.${
        y.middlename match {
          case x: String if x == "" => ""
          case x: String => " " + x.take(1) + "."
        }
      }").getOrElse("")

      val documentRows = ap.apFiles.values.map(_.comments).toSeq.map("—   " + _ + ";")
      val scoresM = ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.scores.map(x => x.medical))
      val scoresE = ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.scores.map(x => x.educational))
      val scoresS = ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.scores.map(x => x.scientific))

      //      println(ap.expertise.bulletins.values.filter(x => x.isFinished).map(_.scores))

      val commentRows = ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).toList.map { bulletin =>
        (
          x.find(_.id == bulletin.expertId).map(u => u.lastname + " " + u.firstname).getOrElse(""),
          bulletin.positiveConclusion.getOrElse(""),
          bulletin.negativeConclusion.getOrElse(""))
      }

      val parameters = Map(
        "C1" -> ap.criterions.get(1).map(_.description.ru).getOrElse(""),
        "C2" -> ap.criterions.get(2).map(_.description.ru).getOrElse(""),
        "C3" -> ap.criterions.get(3).map(_.description.ru).getOrElse(""),
        "C4" -> ap.criterions.get(4).map(_.description.ru).getOrElse(""),
        "C5" -> ap.criterions.get(5).map(_.description.ru).getOrElse(""),
        "Applicant" -> ap.apData.entityName.map(_.ru).getOrElse(""),
        "Participated" -> ap.expertise.experts.size, // сколько участвовало
        "MembersOfTheExpertCouncil" -> allUserRole.map(x => x.roles.contains("expert")).size, //всего экспертов

        "CorrespondToTheGoals" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(1).filter(_.decision == 2)).size,
        "DoesNotCorrespondToTheGoals" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(1).filter(_.decision == 1)).size,

        "TheClaimedActivitiesAreRelevant" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(2).filter(_.decision == 2)).size,
        "TheClaimedActivitiesAreNotRelevant" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(2).filter(_.decision == 1)).size,

        "AreScientificallyBased" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(3).filter(_.decision == 2)).size,
        "AreNotScientificallyBased" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(3).filter(_.decision == 1)).size,

        "AreEconomicallyBased" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(4).filter(_.decision == 2)).size,
        "AreNotEconomicallyBased" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(4).filter(_.decision == 1)).size,

        "ArePracticallyRealizable" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(5).filter(_.decision == 2)).size,
        "AreNotPracticallyRealizable" -> ap.expertise.bulletins.values.filter(x => x.isFinished && x.rejection != Some(true)).flatMap(_.criterions.get(5).filter(_.decision == 1)).size,

        "MedicalActivityScore" -> (scoresM.size match {
          case x if x != 0 => Math.round(scoresM.sum * 100 / scoresM.size).toFloat / 100
          case 0 => 0
        }),
        "EducationActivityScore" -> (scoresE.size match {
          case x if x != 0 => Math.round(scoresE.sum * 100 / scoresE.size).toFloat / 100
          case 0 => 0
        }),
        "ScientificActivityScore" -> (scoresS.size match {
          case x if x != 0 => Math.round(scoresS.sum * 100 / scoresS.size).toFloat / 100
          case 0 => 0
        }),

        "Correspond" -> ap.expertise.bulletins.values.filter(z => z.finalResult == Some(true) && z.isFinished && z.rejection != Some(true)).size,
        "DoesNotCorrespond" -> ap.expertise.bulletins.values.filter(z => z.finalResult == Some(false) && z.isFinished && z.rejection != Some(true)).size,

        "ChairmanOfTheExpertCouncil" -> chairmanName,
        "SecretaryOfTheExpertCouncil" -> secretarName)
      TemplateDataB(
        documentRows = documentRows,
        commentRows = commentRows,
        parameters = parameters)
    }
  }
}
