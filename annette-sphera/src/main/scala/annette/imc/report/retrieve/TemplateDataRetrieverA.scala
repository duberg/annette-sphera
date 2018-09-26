package annette.imc.report.retrieve

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.domain.tenancy.model.{TenantUserRole, User}
import annette.imc.ApsActor
import annette.imc.model._
import annette.imc.report.model.TemplateDataA
import annette.imc.user.ImcUserActor
import annette.imc.user.ImcUserActor.SingleEntry
import annette.imc.user.model.{FullUser, ImcUser}

import scala.concurrent.{ExecutionContext, Future}

trait TemplateDataRetrieverA {
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

  private def getUsersAll: Future[Set[User]] = coreModule.userManager.selectAll.map(_.toSet)

  private def getUserRoleAll: Future[Set[TenantUserRole]] = coreModule.tenantUserRoleDao.selectAll.map(_.toSet)

  def retrieveDataA(apId: Ap.Id, language: String): Future[TemplateDataA] = {
    for {
      allUsers: Set[User] <- getUsersAll
      allUserRole: Set[TenantUserRole] <- getUserRoleAll
      allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
      ap: Ap <- getApById(apId)
    } yield {
      val rejectors =  ap.expertise.bulletins.values.filter(a => {
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
      val directorName = (ap.apData.personName.map(_.ru).getOrElse("")).split(",").toList match {
        case Nil => ""
        case f::Nil => f
        case f::n::Nil => f + " " + n.take(1) + "."
        case f::n::m::Nil => f + " " + n.take(1) + "." + " " + m.take(1) + "."
        case _ =>
      }

      val listC: Seq[String] = x.filter(y => {
        ap.expertise.experts.contains(y.id)
      }).map(y => s"${y.lastName} ${y.firstName.take(1)}.${
        y.middleName match {
          case x if x == "" => ""
          case x => x.take(1) + "."
        }
      }").toSeq


      val directorPosition = ap.apData.personPosition.map(_.ru).getOrElse("")

      val secretar = x.find(_.secretar)
      val secretarName = secretar.map(y => s"${y.lastName} ${y.firstName.take(1)}.${
        y.middleName match {
          case x if x == "" => ""
          case x => x.take(1) + "."
        }
      }").getOrElse("")

      val presentRows = Seq(
        (s"Председатель Экспертного совета, $chairmanPosition", "-", chairmanName),
      )
      val membersOfTheExpertCouncilRows =
        x.filter(z => {
          ap.expertise.experts.contains(z.id)
        })
          .map { y =>
            val p = y.position.getOrElse("") + " " + y.rank.getOrElse("")
            val n = s"${y.lastName} ${y.firstName.take(1)}.${y.middleName match {
              case x: String if x == "" => ""
              case x: String => " " + x.take(1) + "."
            }}"
            (p, "-", n)
          }

      val RejList = x.filter(z => rejectors.contains(z.id)).map(_.lastName)

      val RejectString = RejList.size match {
        case 0 => ""
        case 1 => "Эксперт " + RejList.head + " заявил самоотвод."
        case _ => "Следующие эксперты заявили самоотвод: " + RejList.mkString(", ")
      }

      val inviteesRows = Seq.empty
      val apExperts = x.filter(y => ap.expertise.experts.contains(y.id))

      val listA = x.filter(_.expert).map(_.lastName) // все эксперты
      val listB = apExperts.withFilter(e => e.expert || e.additional ).map(_.lastName) // вовлеченные в заявку эксперты

      val parameters: Map[String, Any] = Map(
        "Rejectors" -> RejectString,
        "Applicant" -> applicant,
        "DirectorPosition" -> directorPosition,
        "DirectorName" -> directorName,
        "ChairmanOfTheExpertCouncil" -> chairmanName,
        "SecretaryOfTheExpertCouncil" -> secretarName,
        "MembersOfTheExpertCouncil" -> listC.size,
        "CompletedBallots1" -> ap.expertise.bulletins.values.count(y => y.isFinished && y.finalResult.isDefined),
        "CompletedBallots2" -> 14, //не нашел, что это?
        "ListA" -> listA,
        "ListB" -> listC)

      TemplateDataA(
        presentRows = presentRows,
        membersOfTheExpertCouncilRows = membersOfTheExpertCouncilRows.toSeq,
        inviteesRows = inviteesRows,
        parameters = parameters)
    }
  }
}