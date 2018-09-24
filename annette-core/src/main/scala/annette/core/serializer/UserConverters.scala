package annette.core.serializer

import java.time.ZonedDateTime
import java.util.UUID

import annette.core.domain.application.model.Application
import annette.core.domain.tenancy.model._
import annette.core.domain.tenancy.{ UserService, actor }
import annette.core.domain.tenancy.actor.UsersState
import annette.core.serializer.proto.user._
import Implicits._

trait UserConverters {

  val CreatedUserEvtManifestV1 = "CreatedUserEvt.v1"
  val UpdatedUserEvtManifestV1 = "UpdatedUserEvt.v1"
  val DeletedUserEvtManifestV1 = "DeletedUserEvt.v1"
  val UsersStateManifestV1 = "UsersState.v1"

  def toCreatedUserEvtBinary(obj: UserService.CreatedUserEvt) = {
    CreatedUserEvtV1(obj.x).toByteArray
  }

  def toUpdateUserEvtBinary(obj: UserService.UpdatedUserEvt): Array[Byte] = {
    UpdatedUserEvtV1(obj.x).toByteArray
  }

  def toDeleteUserEvtBinary(obj: UserService.DeletedUserEvt) = {
    DeletedUserEvtV1(obj.userId).toByteArray
  }

  def toUserStatesBinary(obj: UsersState): Array[Byte] = {
    UsersStateV1(
      users = obj.users.map({ case (x, y) => UUIDToString(x) -> fromUser(y) }),
      emailIndex = obj.emailIndex,
      phoneIndex = obj.phoneIndex,
      usernameIndex = obj.usernameIndex,
      userProperties = obj.userProperties.map({ case (x, y) => x.toString -> fromUserProperty(y) })).toByteArray
  }

  def fromCreatedUserEvtV1(bytes: Array[Byte]): UserService.CreatedUserEvt = {
    val x = CreatedUserEvtV1.parseFrom(bytes).x
    UserService.CreatedUserEvt(x)
  }

  def fromUpdatedUserEvtV1(bytes: Array[Byte]): UserService.UpdatedUserEvt = {
    val x = UpdatedUserEvtV1.parseFrom(bytes).x
    UserService.UpdatedUserEvt(x)
  }

  def fromDeletedUserEvtV1(bytes: Array[Byte]): UserService.DeletedUserEvt = {
    val userId = DeletedUserEvtV1.parseFrom(bytes).userId
    UserService.DeletedUserEvt(userId)
  }

  def fromUsersStateV1(bytes: Array[Byte]): UsersState = {
    val x = UsersStateV1.parseFrom(bytes)
    UsersState(
      users = x.users.map({ case (a, b) => stringToUUID(a) -> toUser(b) }),
      emailIndex = x.emailIndex,
      phoneIndex = x.phoneIndex,
      usernameIndex = x.usernameIndex,
      userProperties = x.userProperties.map({ case (a, b) => UserProperty.Id(a) -> toUserProperty(b) }))
  }

  implicit def toUser(x: UserV1): User = {
    User(
      id = x.id,
      username = x.username,
      name = x.name,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      locale = x.locale,
      registeredDate = x.registeredDate,
      //tenants = x.tenants.toSet,
      //applications = x.applications,
      //roles = x.roles,
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      deactivated = x.deactivated)
  }

  implicit def fromUser(x: User): UserV1 = {
    UserV1(
      id = x.id,
      username = x.username,
      name = x.name,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      locale = x.locale,
      registeredDate = x.registeredDate,
      //tenants = x.tenants.toSet,
      //applications = x.applications,
      //roles = x.roles,
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      deactivated = x.deactivated)
  }

  implicit def toUpdateUser(x: UpdateUserV1): UpdateUser = {
    UpdateUser(
      id = x.id,
      username = x.username,
      name = x.name,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      locale = x.locale,
      //tenants = x.tenants,
      //applications = x.applications,
      //roles = x.roles,
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      deactivated = x.deactivated)
  }

  implicit def fromUpdateUser(x: UpdateUser): UpdateUserV1 = {
    UpdateUserV1(
      id = x.id,
      username = x.username,
      name = x.name,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      locale = x.locale,
      //tenants = x.tenants,
      //applications = x.applications,
      //roles = x.roles,
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      deactivated = x.deactivated)
  }

  implicit def toUserPropertyId(x: UserPropertyIdV1): UserProperty.Id = {
    UserProperty.Id(
      userId = x.userId,
      tenantId = x.tenantId,
      applicationId = x.applicationId,
      key = x.key)
  }

  implicit def fromUserPropertyId(x: UserProperty.Id): UserPropertyIdV1 = {
    UserPropertyIdV1(
      userId = x.userId,
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
