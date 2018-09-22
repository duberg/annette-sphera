package annette.imc.model

case class ApData(
  entityName: Option[ApString] = None,
  personName: Option[ApString] = None,
  personPosition: Option[ApString] = None,
  personEmail: Option[String] = None,
  personTel: Option[String] = None,
  country: Option[String] = None,
  //  operationTypes: Option[Set[OperationType]] = None,
  operationTypes: Option[ApString] = None,
  financing: Option[Set[Financing]] = None,
  isForLong: Option[Boolean] = None,
  purpose: Option[String] = None,
  name: Option[ApString] = None,
  capital: Option[String] = None,
  applicantInfo: Option[ApString] = None,
  address: Option[String] = None)
