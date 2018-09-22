package annette.imc.model

import java.util.UUID

import annette.core.domain.tenancy.model.User

case class Ap(
  id: Ap.Id,
  apData: ApData,
  apStatus: ApStatus,
  history: Seq[ApStatus],
  projectManager: User.Id = UUID.fromString("00000000-0000-0000-0000-000000000000"),
  apFiles: Ap.ApFiles = Map.empty,
  criterions: Map[Int, Criterion] = Map.empty,
  expertise: Expertise = Expertise())

object Ap {
  type Id = UUID
  type ApFiles = Map[ApFile.Id, ApFile]
}

case class Expertise(
  experts: Set[User.Id] = Set.empty,
  bulletins: Map[User.Id, Bulletin] = Map.empty)

case class ShowcaseExpertise(
  experts: Set[User.Id] = Set.empty,
  bulletins: List[Bulletin] = List.empty)

case class UpdateAp(
  id: Ap.Id,
  entityName: Option[ApString] = None,
  personName: Option[ApString] = None,
  personPosition: Option[ApString] = None,
  personEmail: Option[String] = None,
  personTel: Option[String] = None,
  country: Option[String] = None,
  operationTypes: Option[ApString] = None,
  financing: Option[Set[Financing]] = None,
  isForLong: Option[Boolean] = None,
  purpose: Option[String] = None,
  name: Option[ApString] = None,
  capital: Option[String] = None,
  applicantInfo: Option[ApString] = None,
  address: Option[String] = None)

case class SearchParams(
  searchText: Option[String] = None,
  status: Option[String] = None)

case class ApSimple(
  id: Ap.Id,
  nameRu: Some[String],
  nameEn: Some[String],
  status: String,
  projectManager: String)
