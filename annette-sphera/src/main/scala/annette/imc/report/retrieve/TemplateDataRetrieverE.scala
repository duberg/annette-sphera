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
import annette.imc.model._
import annette.imc.report.model.{ TemplateDataA, TemplateDataC }
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{ FullUser, ImcUser }

import scala.concurrent.{ ExecutionContext, Future }

trait TemplateDataRetrieverE {
  implicit def c: ExecutionContext
  implicit def t: Timeout

  val coreModule: CoreModule

  def apsActor: ActorRef
  def imcUserActor: ActorRef

  private def getApById(apId: Ap.Id): Future[Ap] =
    ask(apsActor, ApsActor.GetApById(apId))
      .mapTo[ApsActor.ApFound]
      .map(_.ap)

  private def getUsers(x: Set[User.Id]): Future[Set[User]] =
    Future.sequence(x.map(coreModule.userManager.getById))
      .map(_.flatten)

  private def getImcUser(userId: User.Id): Future[Option[ImcUser]] =
    imcUserActor.ask(ImcUserActor.GetById(userId))
      .mapTo[SingleEntry]
      .map(_.maybeEntry)

  private def getImcUsers(x: Set[User.Id]): Future[Set[ImcUser]] =
    Future.sequence(x.map(getImcUser))
      .map(_.flatten)

  private def getAllImcUsers: Future[Map[UUID, ImcUser]] =
    imcUserActor.ask(ImcUserActor.GetAll).mapTo[ImcUserActor.MultipleEntries].map(_.entries)

  private def getUsersAll: Future[Set[User]] = coreModule.userManager.listUsers.map(_.toSet)

  private def getUserRoleAll: Future[Set[TenantUserRole]] = coreModule.tenantUserRoleDao.selectAll.map(_.toSet)

  def retrieveDataE(apId: Ap.Id, language: String): Future[TemplateDataC] = {
    for {
      allUsers: Set[User] <- getUsersAll
      allUserRole: Set[TenantUserRole] <- getUserRoleAll
      allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
      ap: Ap <- getApById(apId)
    } yield {
      val rejectors = ap.expertise.bulletins.values.filter(a => {
        a.rejection == Some(true)
      }).toSeq.map(_.expertId)
      val x: Set[FullUser] = for (user <- allUsers) yield {
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

      val chairman = x.find(_.chairman)
      val chairmanName = chairman.map(x => s"${x.lastName} ${x.firstName} ${x.middleName}").getOrElse("")
      val chairmanPosition = chairman.flatMap(_.position).getOrElse("")

      val applicant = ap.apData.entityName.map(_.ru).getOrElse("")

      val RejList = x.filter(z => rejectors.contains(z.id)).map(_.lastName)

      val RejectString = RejList.size match {
        case 0 => "0"
        case 1 => "1 член экспертного совета:" + RejList.head + " (особое мнение)"
        case _ => RejList.size + "членов экспертного совета: " + RejList.mkString(", ")
      }

      val RejectString1 = RejList.size match {
        case 0 => ""
        case 1 => "(в том числе с особым мнением: " + RejList.head + ")"
        case _ => "(в том числе с особым мнением: " + RejList.mkString(", ") + ")"
      }

      val experts = x.filter(_.expert)

      val inviteesRows = Seq.empty
      val apExperts = x.filter(y => ap.expertise.experts.contains(y.id))
      val apAdditional = x.filter(y => ap.expertise.experts.contains(y.id) && y.additional)
      val expertsVoted = ap.expertise.bulletins.values.filter(z => z.isFinished && !z.rejection.contains(true) &&
        apExperts.map(_.id).contains(z.expertId))

      val bulletins = ap.expertise.bulletins.values.filter(z => z.isFinished && !z.rejection.contains(true))

      val expertsSize = ap.expertise.bulletins.values.count(!_.rejection.contains(true))
      val votes = ap.expertise.bulletins.values.filter(x => x.isFinished && !x.rejection.contains(true))
      val notVoted = ap.expertise.bulletins.values.filter(x => !x.isFinished
        && !x.rejection.contains(true) && allUsers.map(_.id).contains(x.expertId))

      val pluses = votes.count(_.finalResult.contains(true))
      val quorum: Boolean = if (expertsSize > 0) votes.size.toFloat / expertsSize.toFloat > 0.5 else false

      val result = if (!quorum) "не соответствующми"
      else if (pluses.toFloat / votes.size.toFloat >= 0.67) "соответствующми"
      else "не соответствующми"

      val listA = x.filter(_.expert).map(_.lastName) // все эксперты
      val listB = apExperts.withFilter(e => e.expert || e.additional).map(_.lastName) // вовлеченные в заявку эксперты
      val additionalSize = x.count(_.additional)
      val parameters: Map[String, Any] = Map(
        "Rejectors" -> RejectString,
        "Rejectors1" -> RejectString1,
        "Date" -> LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        "expertsSize" -> apExperts.size,
        "expertsVoted" -> expertsVoted.size,
        "notVoted" -> (apExperts.size - expertsVoted.size),
        "expertsFinish" -> (if (apAdditional.size == 0) "." else s", а также $additionalSize привлеченных  эксперта (из дополнительных экспертов)."),
        "Applicant" -> applicant,
        "bulletins" -> bulletins.size,
        "Correspond" -> ap.expertise.bulletins.values.count(z => z.finalResult == Some(true) && z.isFinished && z.rejection != Some(true)),
        "DoesNotCorrespond" -> ap.expertise.bulletins.values.count(z => z.finalResult == Some(false) && z.isFinished && z.rejection != Some(true)),
        "result" -> result)

      TemplateDataC(

        parameters = parameters)
    }
  }
}