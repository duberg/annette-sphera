package annette.core.domain.tenancy.model

import java.time.ZonedDateTime

import annette.core.domain.application.model.Application

/**
 * Cодержит основные реквизиты пользователя.
  *
  * @param id Unique identifier for the user.
  * @param username Login name for the user.
  * @param name Display name for the user.
  * @param firstName First name for the user.
  * @param lastName Last name for the user.
  * @param middleName
  * @param email The email address for the user.
  * @param url URL of the user.
  * @param description Description of the user.
  * @param phone
  * @param locale Locale for the user.
  * @param registeredDate Registration date for the user.
  * @param roles Roles assigned to the user.
  * @param password Password for the user (never included).
  * @param avatarUrl Avatar URL for the user.
  * @param meta Meta fields.
  */
case class User(
                 id: User.Id,
                 username: Option[String],
                 name: Option[String],
                 firstName: String,
                 lastName: String,
                 middleName: Option[String],
                 email: Option[String],
                 url: Option[String],
                 description: Option[String],
                 phone: Option[String],
                 locale: Option[String],
                 registeredDate: ZonedDateTime,
                 tenants: Set[Tenant.Id],
                 applications: Map[Application.Id, Tenant.Id],
                 roles: Map[String, Tenant.Id],
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

case class CreateUser(
                 username: Option[String],
                 name: Option[String],
                 firstName: String,
                 lastName: String,
                 middleName: Option[String],
                 email: Option[String],
                 url: Option[String],
                 description: Option[String],
                 phone: Option[String],
                 locale: Option[String],
                 roles: Map[String, Tenant.Id],
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
                       name: Option[String],
                       firstName: Option[String],
                       lastName: Option[String],
                       middleName: Option[Option[String]],
                       email: Option[Option[String]],
                       url: Option[Option[String]],
                       description: Option[Option[String]],
                       phone: Option[Option[String]],
                       locale: Option[Option[String]],
                       roles: Option[Map[String, Tenant.Id]],
                       password: Option[String],
                       avatarUrl: Option[Option[String]],
                       sphere: Option[Option[String]],
                       company: Option[Option[String]],
                       position: Option[Option[String]],
                       rank: Option[Option[String]],
                       additionalTel: Option[Option[String]],
                       additionalMail: Option[Option[String]],
                       meta: Option[Map[String, String]],
                       deactivated: Boolean
                     )

object User {
  type Id = Int
}