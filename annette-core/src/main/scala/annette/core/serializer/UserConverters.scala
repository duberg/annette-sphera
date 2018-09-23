package annette.core.serializer

import java.util.UUID

import annette.core.domain.application.model.Application
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.UserService
import annette.core.domain.tenancy.actor.UsersActorState
import annette.core.serializer.proto.user._

trait UserConverters {

  val UserCreatedEvtManifestV1 = "User.CreatedEvt.v1"
  val UserUpdatedEvtManifestV1 = "User.UpdatedEvt.v1"
  val UserDeletedEvtManifestV1 = "User.DeletedEvt.v1"
  val UserStateManifestV1 = "User.State.v1"

  def toUserCreatedEvtBinary(obj: UserService.UserCreatedEvt) = {
    UserCreatedEvtV1(obj.entry, obj.password).toByteArray
  }

  def toUserUpdateEvtBinary(obj: UserService.UserUpdatedEvt): Array[Byte] = {
    UserUpdatedEvtV1(obj.entry).toByteArray
  }

  def toUserDeleteEvtBinary(obj: UserService.UserDeletedEvt) = {
    UserDeletedEvtV1(obj.id.toString).toByteArray
  }

  def toUserStateBinary(obj: UsersActorState): Array[Byte] = {
    UserStateV1(
      userRecs = obj.users.map({ case (x, y) => x.toString -> fromUserRec(y) }),
      emailIndex = obj.emailIndex.mapValues(_.toString),
      phoneIndex = obj.phoneIndex.mapValues(_.toString),
      loginIndex = obj.loginIndex.mapValues(_.toString),
      userProperties = obj.userProperties.map({ case (x, y) => x.toString -> fromUserProperty(y) })).toByteArray
  }

  def fromUserCreatedEvtV1(bytes: Array[Byte]): UserService.UserCreatedEvt = {
    val x = UserCreatedEvtV1.parseFrom(bytes)
    UserService.UserCreatedEvt(x.entry, x.password)
  }

  def fromUserUpdatedEvtV1(bytes: Array[Byte]): UserService.UserUpdatedEvt = {
    val x = UserUpdatedEvtV1.parseFrom(bytes)
    UserService.UserUpdatedEvt(x.entry)
  }

  def fromUserDeletedEvtV1(bytes: Array[Byte]): UserService.UserDeletedEvt = {
    val id = UUID.fromString(UserDeletedEvtV1.parseFrom(bytes).id)
    UserService.UserDeletedEvt(id)
  }

  def fromUserStateV1(bytes: Array[Byte]): UsersActorState = {
    val p = UserStateV1.parseFrom(bytes)
    UserState(
      userRecs = p.userRecs.map({ case (x, y) => UUID.fromString(x) -> toUserRec(y) }),
      emailIndex = p.emailIndex.mapValues(UUID.fromString),
      phoneIndex = p.phoneIndex.mapValues(UUID.fromString),
      loginIndex = p.loginIndex.mapValues(UUID.fromString),
      userProperties = p.userProperties.map({ case (x, y) => UserProperty.Id(x) -> toUserProperty(y) }))
  }

  implicit def toUser(x: UserV1): User =
    User(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = x.email,
      phone = x.phone,
      username = x.login,
      defaultLanguage = x.defaultLanguage,
      id = UUID.fromString(x.id))

  implicit def fromUser(x: User): UserV1 = {
    UserV1(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = x.email,
      phone = x.phone,
      login = x.username,
      defaultLanguage = x.defaultLanguage,
      id = x.id.toString)
  }

  implicit def toUserUpdate(x: UserUpdateV1): UpdateUser = {
    UserUpdate(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = Some(x.email),
      phone = Some(x.phone),
      login = Some(x.login),
      defaultLanguage = x.defaultLanguage,
      id = UUID.fromString(x.id))
  }

  implicit def fromUserUpdate(x: UpdateUser): UserUpdateV1 = {
    UserUpdateV1(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = x.email.get,
      phone = x.phone.get,
      login = x.login.get,
      defaultLanguage = x.defaultLanguage,
      id = x.id.toString)
  }

  implicit def toUserRec(x: UserRecV1): UserRec = {
    UserRec(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = x.email,
      phone = x.phone,
      login = x.login,
      defaultLanguage = x.defaultLanguage,
      password = x.password,
      id = UUID.fromString(x.id))
  }

  implicit def fromUserRec(x: UserRec): UserRecV1 = {
    UserRecV1(
      lastname = x.lastname,
      firstname = x.firstname,
      middlename = x.middlename,
      email = x.email,
      phone = x.phone,
      login = x.login,
      defaultLanguage = x.defaultLanguage,
      password = x.password,
      id = x.id.toString)
  }

  implicit def toUserPropertyId(x: UserPropertyIdV1): UserProperty.Id = {
    UserProperty.Id(
      userId = UUID.fromString(x.userId),
      tenantId = x.tenantId,
      applicationId = x.applicationId,
      key = x.key)
  }

  implicit def fromUserPropertyId(x: UserProperty.Id): UserPropertyIdV1 = {
    UserPropertyIdV1(
      userId = x.userId.toString,
      tenantId = x.tenantId,
      applicationId = x.applicationId,
      key = x.key)
  }

  implicit def toUserProperty(x: UserPropertyV1): UserProperty = {
    UserProperty(
      id = x.id,
      value = x.value)
  }

  implicit def fromUserProperty(x: UserProperty): UserPropertyV1 = {
    UserPropertyV1(
      id = x.id,
      value = x.value)
  }
}
