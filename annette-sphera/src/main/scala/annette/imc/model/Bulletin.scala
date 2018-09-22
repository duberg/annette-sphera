package annette.imc.model

import java.time.ZonedDateTime

import annette.core.domain.tenancy.model.User

case class Vote(
  decision: Int,
  pluses: String = "",
  minuses: String = "")

case class Scores(medical: Int, educational: Int, scientific: Int)

case class Bulletin(
  expertId: User.Id,
  date: ZonedDateTime,
  criterions: Map[Int, Vote],
  finalResult: Option[Boolean],
  positiveConclusion: Option[String],
  negativeConclusion: Option[String],
  rejection: Option[Boolean],
  scores: Option[Scores],
  isFinished: Boolean = false)

case class UpdateBulletin(
  expertId: User.Id,
  criterions: Option[Map[Int, Vote]] = None,
  finalResult: Option[Boolean] = None,
  positiveConclusion: Option[String] = None,
  negativeConclusion: Option[String] = None,
  rejection: Option[Boolean] = None,
  scores: Option[Scores] = None,
  isFinished: Option[Boolean] = None)

//case class Bulletin(
//                     expertId: User.Id,
//                     date: ZonedDateTime,
//                     applicant: ApString,
//                     rejection: Option[ApString],
//                     criterions: Map[Int, Vote],
//                     finalResult: Option[Vote],
//                     scores: Option[Scores],
//                     isFinished: Boolean = false)
