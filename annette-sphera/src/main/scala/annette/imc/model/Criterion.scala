package annette.imc.model

case class Criterion(
  id: Int,
  description: ApString,
  attachment: Set[ApFile.Id] = Set.empty,
  isFinished: Boolean = false)

case class UpdateCriterion(
  id: Int,
  descriptionRu: Option[String] = None,
  descriptionEn: Option[String] = None)
