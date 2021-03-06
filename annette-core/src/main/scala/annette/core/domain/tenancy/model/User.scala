package annette.core.domain.tenancy.model

import java.time.ZonedDateTime
import java.util.UUID

import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.akkaext.http.PageRequest
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
  gender: Option[String],
  email: Option[String],
  url: Option[String],
  description: Option[String],
  phone: Option[String],
  language: Option[String],
  registeredDate: ZonedDateTime,
  roles: Map[Tenant.Id, Set[Role.Id]],
  password: String,
  avatarUrl: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  additionalTel: Option[String],
  additionalMail: Option[String],
  meta: Map[String, String],
  status: Int)

// todo: add dateOfBbirth

case class SignUpUser(
  email: String,
  firstName: String,
  lastName: String,
  password: String,
  language: Option[String],
  tenants: Set[Tenant.Id])

case class PaginateUsers(items: List[User], totalCount: Int)

case class CreateUser(
  id: Option[User.Id] = None,
  username: Option[String],
  displayName: Option[String],
  firstName: String,
  lastName: String,
  middleName: Option[String],
  gender: Option[String],
  email: Option[String],
  url: Option[String],
  description: Option[String],
  phone: Option[String],
  language: Option[String],
  roles: Option[Map[Tenant.Id, Set[Role.Id]]],
  password: String,
  avatarUrl: Option[String],
  sphere: Option[String],
  company: Option[String],
  position: Option[String],
  rank: Option[String],
  additionalTel: Option[String],
  additionalMail: Option[String],
  meta: Option[Map[String, String]],
  status: Option[Int])

case class UpdateUser(
  id: User.Id,
  username: Option[Option[String]],
  displayName: Option[Option[String]],
  firstName: Option[String],
  lastName: Option[String],
  middleName: Option[Option[String]],
  gender: Option[Option[String]],
  email: Option[Option[String]],
  url: Option[Option[String]],
  description: Option[Option[String]],
  phone: Option[Option[String]],
  language: Option[Option[String]],
  roles: Option[Map[Tenant.Id, Set[Role.Id]]],
  password: Option[String],
  avatarUrl: Option[Option[String]],
  sphere: Option[Option[String]],
  company: Option[Option[String]],
  position: Option[Option[String]],
  rank: Option[Option[String]],
  additionalTel: Option[Option[String]],
  additionalMail: Option[Option[String]],
  meta: Option[Map[String, String]],
  status: Option[Int])

object User {
  type Id = UUID

  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case class CreateUserCmd(x: CreateUser) extends Command
  case class UpdateUserCmd(x: UpdateUser) extends Command
  case class DeleteUserCmd(userId: User.Id) extends Command
  case class UpdatePasswordCmd(userId: User.Id, password: String) extends Command

  case class GetUserById(id: User.Id) extends Query
  case class GetUserByLoginAndPassword(login: String, password: String) extends Query
  object ListUsers extends Query
  case class PaginateListUsers(page: PageRequest) extends Query

  case class CreatedUserEvt(x: User) extends Event
  case class UpdatedUserEvt(x: UpdateUser) extends Event
  case class UpdatedPasswordEvt(userId: User.Id, password: String) extends Event
  case class DeletedUserEvt(userId: User.Id) extends Event
  case class ActivatedUserEvt(userId: User.Id) extends Event

  case class CreateUserSuccess(x: User) extends Response
  case class UserOpt(maybeEntry: Option[User]) extends Response
  case class UsersMap(x: Map[User.Id, User]) extends Response
  case class UsersList(x: PaginateUsers) extends Response
}