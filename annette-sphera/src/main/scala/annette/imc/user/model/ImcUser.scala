package annette.imc.user.model

import annette.core.domain.tenancy.model.User

case class ImcUser(
  id: User.Id,
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  postponed: Boolean = false,
  otherTel: Option[String] = None,
  otherMail: Option[String] = None)

case class UpdateUser(
  user: PreUser,
  password: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  otherTel: Option[String] = None,
  otherMail: Option[String] = None)

case class UpdatedUser(
  id: User.Id,
  user: PreUser,
  password: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  otherTel: Option[String] = None,
  otherMail: Option[String] = None)

case class PreUser(
  lastname: String,
  firstname: String,
  middlename: String,
  email: Option[String],
  phone: Option[String],
  defaultLanguage: String = "RU")

case class UserRoled(
  id: User.Id,
  lastName: String,
  firstName: String,
  middleName: String,
  email: Option[String] = None,
  admin: Boolean = false,
  secretar: Boolean = false,
  manager: Boolean = false,
  chairman: Boolean = false,
  expert: Boolean = false,
  additional: Boolean = false)

case class FullUser(
  id: User.Id,
  lastname: String,
  firstname: String,
  middlename: String,
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  admin: Boolean = false,
  secretar: Boolean = false,
  manager: Boolean = false,
  chairman: Boolean = false,
  expert: Boolean = false,
  additional: Boolean = false)