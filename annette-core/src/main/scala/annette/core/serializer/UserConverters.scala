package annette.core.serializer

import annette.core.domain.authorization.model.Role
import annette.core.domain.tenancy.actor.UserManagerState
import annette.core.domain.tenancy.model.User._
import annette.core.domain.tenancy.model._
import annette.core.serializer.Implicits._
import annette.core.serializer.proto.user._

trait UserConverters {
  val CreatedUserEvtManifestV1 = "CreatedUserEvt.v1"
  val UpdatedUserEvtManifestV1 = "UpdatedUserEvt.v1"
  val DeletedUserEvtManifestV1 = "DeletedUserEvt.v1"
  val UsersStateManifestV1 = "UsersState.v1"

  def toCreatedUserEvtBinary(obj: CreatedUserEvt) = {
    CreatedUserEvtV1(fromUser(obj.x)).toByteArray
  }

  def toUpdateUserEvtBinary(obj: UpdatedUserEvt): Array[Byte] = {
    UpdatedUserEvtV1(fromUpdateUser(obj.x)).toByteArray
  }

  def toDeleteUserEvtBinary(obj: DeletedUserEvt) = {
    DeletedUserEvtV1(obj.userId).toByteArray
  }

  def toUserStatesBinary(obj: UserManagerState): Array[Byte] = {
    UsersStateV1(
      users = obj.users.map({ case (x, y) => UUIDToString(x) -> fromUser(y) }),
      emailIndex = obj.emailIndex,
      phoneIndex = obj.phoneIndex,
      usernameIndex = obj.usernameIndex,
      userProperties = obj.userProperties.map({ case (x, y) => x.toString -> fromUserProperty(y) })).toByteArray
  }

  def fromCreatedUserEvt(bytes: Array[Byte]): CreatedUserEvt = {
    val x = CreatedUserEvtV1.parseFrom(bytes).x
    CreatedUserEvt(toUser(x))
  }

  def fromUpdatedUserEvt(bytes: Array[Byte]): UpdatedUserEvt = {
    val x = UpdatedUserEvtV1.parseFrom(bytes).x
    UpdatedUserEvt(toUpdateUser(x))
  }

  def fromDeletedUserEvt(bytes: Array[Byte]): DeletedUserEvt = {
    val userId = DeletedUserEvtV1.parseFrom(bytes).userId
    DeletedUserEvt(userId)
  }

  def fromUsersStateV1(bytes: Array[Byte]): UserManagerState = {
    val x = UsersStateV1.parseFrom(bytes)
    UserManagerState(
      users = x.users.map({ case (a, b) => stringToUUID(a) -> toUser(b) }),
      emailIndex = x.emailIndex,
      phoneIndex = x.phoneIndex,
      usernameIndex = x.usernameIndex,
      userProperties = x.userProperties.map({ case (a, b) => UserProperty.Id(a) -> toUserProperty(b) }))
  }

  def toUser(x: UserV1): User = {
    User(
      id = x.id,
      username = x.username,
      displayName = x.displayName,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      gender = x.gender,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      language = x.language,
      registeredDate = x.registeredDate,
      roles = x.roles.mapValues(toRoles),
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      status = x.status)
  }

  def fromUser(x: User): UserV1 = {
    UserV1(
      id = x.id,
      username = x.username,
      displayName = x.displayName,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName,
      email = x.email,
      url = x.url,
      description = x.description,
      phone = x.phone,
      language = x.language,
      registeredDate = x.registeredDate.toString,
      roles = x.roles.mapValues(fromRoles),
      password = x.password,
      avatarUrl = x.avatarUrl,
      sphere = x.sphere,
      company = x.company,
      position = x.position,
      rank = x.rank,
      additionalTel = x.additionalTel,
      additionalMail = x.additionalMail,
      meta = x.meta,
      status = x.status)
  }

  def toRoles(x: RolesV1): Set[Role.Id] = x.x.toSet

  def fromRoles(x: Set[Role.Id]): RolesV1 = RolesV1(x.toSeq)

  def toRolesMap(x: RolesMapV1): Map[Tenant.Id, Set[Role.Id]] = {
    x.x.mapValues(toRoles)
  }

  def fromRolesMap(x: Map[Tenant.Id, Set[Role.Id]]): RolesMapV1 = {
    RolesMapV1(x.mapValues(fromRoles))
  }

  def toMetaMap(x: MetaMapV1): Map[String, String] = {
    x.x
  }

  def fromMetaMap(x: Map[String, String]): MetaMapV1 = {
    MetaMapV1(x)
  }

  def toOptionRolesMap(x: OptionRolesMapV1): Option[Map[Tenant.Id, Set[Role.Id]]] = {
    x.optionOneof.opt2 match {
      case Some(_) => None
      case None => x.optionOneof.opt1.map(toRolesMap)
    }
  }

  def fromOptionRolesMap(x: Option[Map[Tenant.Id, Set[Role.Id]]]): OptionRolesMapV1 = {
    x match {
      case Some(y) => OptionRolesMapV1.defaultInstance.withOpt1(fromRolesMap(y))
      case None => OptionRolesMapV1.defaultInstance.withOpt2(true)
    }
  }

  def toOptionMetaMap(x: OptionMetaMapV1): Option[Map[String, String]] = {
    x.optionOneof.opt2 match {
      case Some(_) => None
      case None => x.optionOneof.opt1.map(toMetaMap)
    }
  }

  def fromOptionMetaMap(x: Option[Map[String, String]]): OptionMetaMapV1 = {
    x match {
      case Some(y) => OptionMetaMapV1.defaultInstance.withOpt1(fromMetaMap(y))
      case None => OptionMetaMapV1.defaultInstance.withOpt2(true)
    }
  }

  def toUpdateUser(x: UpdateUserV1): UpdateUser = {
    UpdateUser(
      id = x.id,
      username = x.username.map(Some.apply),
      displayName = x.displayName.map(Some.apply),
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName.map(Some.apply),
      gender = x.gender.map(Some.apply),
      email = x.email.map(Some.apply),
      url = x.url.map(Some.apply),
      description = x.description.map(Some.apply),
      phone = x.phone.map(Some.apply),
      language = x.language.map(Some.apply),
      roles = toOptionRolesMap(x.roles),
      password = x.password,
      avatarUrl = x.avatarUrl.map(Some.apply),
      sphere = x.sphere.map(Some.apply),
      company = x.company.map(Some.apply),
      position = x.position.map(Some.apply),
      rank = x.rank.map(Some.apply),
      additionalTel = x.additionalTel.map(Some.apply),
      additionalMail = x.additionalMail.map(Some.apply),
      meta = toOptionMetaMap(x.meta),
      status = x.status)
  }

  def fromUpdateUser(x: UpdateUser): UpdateUserV1 = {
    UpdateUserV1(
      id = x.id,
      username = x.username.flatten,
      displayName = x.displayName.flatten,
      firstName = x.firstName,
      lastName = x.lastName,
      middleName = x.middleName.flatten,
      gender = x.gender.flatten,
      email = x.email.flatten,
      url = x.url.flatten,
      description = x.description.flatten,
      phone = x.phone.flatten,
      language = x.language.flatten,
      roles = fromOptionRolesMap(x.roles),
      password = x.password,
      avatarUrl = x.avatarUrl.flatten,
      sphere = x.sphere.flatten,
      company = x.company.flatten,
      position = x.position.flatten,
      rank = x.rank.flatten,
      additionalTel = x.additionalTel.flatten,
      additionalMail = x.additionalMail.flatten,
      meta = fromOptionMetaMap(x.meta),
      status = x.status)
  }

  def toUserPropertyId(x: UserPropertyIdV1): UserProperty.Id = {
    UserProperty.Id(
      userId = x.userId,
      tenantId = x.tenantId,
      applicationId = x.applicationId,
      key = x.key)
  }

  def fromUserPropertyId(x: UserProperty.Id): UserPropertyIdV1 = {
    UserPropertyIdV1(
      userId = x.userId,
      tenantId = x.tenantId,
      applicationId = x.applicationId,
      key = x.key)
  }

  def toUserProperty(x: UserPropertyV1): UserProperty = {
    UserProperty(
      id = toUserPropertyId(x.id),
      value = x.value)
  }

  def fromUserProperty(x: UserProperty): UserPropertyV1 = {
    UserPropertyV1(
      id = fromUserPropertyId(x.id),
      value = x.value)
  }
}
