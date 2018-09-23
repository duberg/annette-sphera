//package annette.imc.report
//
//import java.time.LocalDate
//
//import annette.core.CoreModule
//import annette.core.akkaguice.AkkaModule
//import annette.core.test.PersistenceSpec
//import annette.imc.model.Ap
//import annette.imc.report.model.{ TemplateDataA, TemplateDataB, TemplateDataC }
//import annette.imc.{ NewAps, NewImcUser }
//import com.google.inject.Guice
//
//import scala.concurrent.Future
//import com.google.inject.Guice
//import com.typesafe.config.{ Config, ConfigException }
//import net.codingwell.scalaguice.InjectorExtensions._
//
//trait NewReportService extends NewAps with NewImcUser { _: PersistenceSpec =>
//  val presentRows = Seq(("Председатель совета директоров", "-", userFullname(userA)))
//
//  val membersOfTheExpertCouncilRows: Seq[(String, String, String)] =
//    imcUsers
//      .zip(users)
//      .map {
//        case (x, y) => (x.sphere.get, "-", userFullname(y))
//      }
//      .toSeq
//
//  val list: Set[String] = users map userShortname
//
//  val parameters = Map(
//    "Applicant" -> "ООО Медицинские технологиии",
//    "DirectorPosition" -> "Генеральный директор",
//    "DirectorName" -> userFullname(userA),
//    "ChairmanOfTheExpertCouncil" -> userFullname(userD),
//    "SecretaryOfTheExpertCouncil" -> userFullname(userC),
//    "MembersOfTheExpertCouncil" -> membersOfTheExpertCouncilRows.size,
//    "CompletedBallots1" -> membersOfTheExpertCouncilRows.size,
//    "CompletedBallots2" -> membersOfTheExpertCouncilRows.size,
//    "ListA" -> list,
//    "ListB" -> list,
//    "Participated" -> membersOfTheExpertCouncilRows.size,
//    "Rejectors" -> generateString(12),
//    "C1Pluses" -> generateInt(1, 10),
//    "C1Minuses" -> generateInt(1, 10),
//    "C2Pluses" -> generateInt(1, 10),
//    "C2Minuses" -> generateInt(1, 10),
//    "C3Pluses" -> generateInt(1, 10),
//    "C3Minuses" -> generateInt(1, 10),
//    "C4Pluses" -> generateInt(1, 10),
//    "C4Minuses" -> generateInt(1, 10),
//    "C5Pluses" -> generateInt(1, 10),
//    "C5Minuses" -> generateInt(1, 10),
//    "CorrespondToTheGoals" -> generateInt(1, 10),
//    "DoesNotCorrespondToTheGoals" -> generateInt(1, 10),
//    "TheClaimedActivitiesAreRelevant" -> generateInt(1, 10),
//    "TheClaimedActivitiesAreNotRelevant" -> generateInt(1, 10),
//    "AreScientificallyBased" -> generateInt(1, 10),
//    "AreNotScientificallyBased" -> generateInt(1, 10),
//    "AreEconomicallyBased" -> generateInt(1, 10),
//    "AreNotEconomicallyBased" -> generateInt(1, 10),
//    "ArePracticallyRealizable" -> generateInt(1, 10),
//    "AreNotPracticallyRealizable" -> generateInt(1, 10),
//    "MedicalActivityScore" -> generateInt(1, 10),
//    "EducationActivityScore" -> generateInt(1, 10),
//    "ScientificActivityScore" -> generateInt(1, 10),
//    "Correspond" -> generateInt(1, 10),
//    "DoesNotCorrespond" -> generateInt(1, 10),
//    "ChairmanOfTheExpertCouncil" -> generateInt(1, 10),
//    "SecretaryOfTheExpertCouncil" -> generateInt(1, 10),
//    "Expert" -> userFullname(userD),
//    "Date" -> LocalDate.now(),
//    "Reason" -> generateString(200),
//    "Criterion1.ExpertOpinion" -> "ДА",
//    "Criterion2.ExpertOpinion" -> "НЕТ",
//    "Criterion3.ExpertOpinion" -> "ДА",
//    "Criterion4.ExpertOpinion" -> "НЕТ",
//    "Criterion5.ExpertOpinion" -> "ДА",
//    "ExpertSummaryComments.StrengthsOfTheApplication" -> generateString(200),
//    "ExpertSummaryComments.WeaknessesOfTheApplication" -> generateString(200),
//    "Result" -> generateString(15),
//    "MedicalActivityScore" -> generateInt(6, 10),
//    "EducationActivityScore" -> generateInt(6, 10),
//    "ScientificActivityScore" -> generateInt(6, 10),
//    "ExpertsPluses" -> generateString(200),
//    "ExpertsMinuses" -> generateString(200))
//
//  val templateDataA = TemplateDataA(
//    presentRows,
//    membersOfTheExpertCouncilRows,
//    membersOfTheExpertCouncilRows,
//    parameters)
//
//  val documentRows = Seq(
//    generateFileName("txt"),
//    generateFileName("docx"),
//    generateFileName("pdf"),
//    generateFileName("pem"))
//
//  val commentRows: Seq[(String, String, String)] =
//    list
//      .map { x =>
//        (x, generateString(200), generateString(200))
//      }
//      .toSeq
//
//  val templateDataB = TemplateDataB(
//    documentRows,
//    commentRows,
//    parameters)
//
//  val templateDataC = TemplateDataC(
//    parameters)
//
//  def newReportService: Future[(Ap.Id, ReportService)] = {
//    val injector = Guice.createInjector(new AkkaModule())
//
//    val coreModule = injector.instance[CoreModule]
//
//    for {
//      imcUserActor <- newFilledImcUser
//      (apId, apsActor) <- newFilledAps(users.head.id)
//    } yield (apId, new ReportService(coreModule, apsActor, imcUserActor))
//  }
//}
