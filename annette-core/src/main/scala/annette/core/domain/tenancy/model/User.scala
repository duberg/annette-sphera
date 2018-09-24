package annette.core.domain.tenancy.model

import java.time.ZonedDateTime
import java.util.{ Locale, UUID }

import annette.core.domain.application.model.Application
import annette.core.domain.authorization.model.Role

/**
 * Cодержит основные реквизиты пользователя.
 *
 * @param id Unique identifier for the user.
 * @param username Login name for the user.
 * @param displayName Display name for the user.
 * @param firstName First name for the user.
 * @param lastName Last name for the user.
 * @param middleName
 * @param email The email address for the user.
 * @param url URL of the user.
 * @param description Description of the user.
 * @param phone
 * @param language Locale for the user.
 * @param registeredDate Registration date for the user.
 * @param roles Roles assigned to the user.
 * @param password Password for the user (never included).
 * @param avatarUrl Avatar URL for the user.
 * @param meta Meta fields.
 */
case class User(
  id: User.Id,
  username: Option[String],
  displayName: Option[String],
  firstName: String,
  lastName: String,
  middleName: Option[String],
  email: Option[String],
  url: Option[String],
  description: Option[String],
  phone: Option[String],
  language: Option[String],
  registeredDate: ZonedDateTime,
  //tenants: Set[Tenant.Id],
  //applications: Map[Application.Id, Set[Tenant.Id]],
  //roles: Map[Role.Id, Set[Tenant.Id]],
  password: String,
  avatarUrl: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  additionalTel: Option[String],
  additionalMail: Option[String],
  meta: Map[String, String],
  deactivated: Boolean) {
}

case class SignUpUser(
  email: String,
  firstName: String,
  lastName: String,
  password: String,
  language: Option[String],
  tenants: Set[Tenant.Id])

case class CreateUser(
  username: Option[String],
  displayName: Option[String],
  firstName: String,
  lastName: String,
  middleName: Option[String],
  email: Option[String],
  url: Option[String],
  description: Option[String],
  phone: Option[String],
  language: Option[String],
  //tenants: Set[Tenant.Id],
  //applications: Map[Application.Id, Set[Tenant.Id]],
  //roles: Map[Role.Id, Set[Tenant.Id]],
  password: String,
  avatarUrl: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  additionalTel: Option[String],
  additionalMail: Option[String],
  meta: Map[String, String],
  deactivated: Boolean)

case class UpdateUser(
  id: User.Id,
  username: Option[Option[String]],
  displayName: Option[Option[String]],
  firstName: Option[String],
  lastName: Option[String],
  middleName: Option[Option[String]],
  email: Option[Option[String]],
  url: Option[Option[String]],
  description: Option[Option[String]],
  phone: Option[Option[String]],
  language: Option[Option[String]],
  //tenants: Option[Set[Tenant.Id]],
  //applications: Option[Map[Application.Id, Set[Tenant.Id]]],
  //roles: Option[Map[Role.Id, Set[Tenant.Id]]],
  password: Option[String],
  avatarUrl: Option[Option[String]],
  sphere: Option[Option[String]],
  company: Option[Option[String]],
  position: Option[Option[String]],
  rank: Option[Option[String]],
  additionalTel: Option[Option[String]],
  additionalMail: Option[Option[String]],
  meta: Option[Map[String, String]],
  deactivated: Option[Boolean])

object User {
  type Id = UUID
}