package annette.core.serializer

import akka.serialization.SerializerWithStringManifest
import annette.core.domain.tenancy.UserService
import annette.core.domain.application.model._
import annette.core.domain.application.{ApplicationService, ApplicationState}
import annette.core.domain.tenancy.actor.UsersActorState

class CoreSerializer extends SerializerWithStringManifest
  with UserConverters
  with ApplicationConverters {

  private val className = Option(this.getClass.getName).getOrElse("<undefined>")

  override def identifier: Int = 20170922

  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case obj: UserService.UserCreatedEvt => toUserCreatedEvtBinary(obj)
      case obj: UserService.UserUpdatedEvt => toUserUpdateEvtBinary(obj)
      case obj: UserService.UserDeletedEvt => toUserDeleteEvtBinary(obj)
      case obj: UsersActorState => toUserStateBinary(obj)

      case obj: ApplicationService.ApplicationCreatedEvt => toApplicationCreatedEvtBinary(obj)
      case obj: ApplicationService.ApplicationUpdatedEvt => toApplicationUpdateEvtBinary(obj)
      case obj: ApplicationService.ApplicationDeletedEvt => toApplicationDeleteEvtBinary(obj)
      case obj: ApplicationState => toApplicationStateBinary(obj)

      case _ =>
        val errorMsg = s"Can't serialize an object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def manifest(o: AnyRef): String = {
    o match {
      case _: UserService.UserCreatedEvt => UserCreatedEvtManifestV1
      case _: UserService.UserUpdatedEvt => UserUpdatedEvtManifestV1
      case _: UserService.UserDeletedEvt => UserDeletedEvtManifestV1
      case _: UsersActorState => UserStateManifestV1

      case _: ApplicationService.ApplicationCreatedEvt => ApplicationCreatedEvtManifestV1
      case _: ApplicationService.ApplicationUpdatedEvt => ApplicationUpdatedEvtManifestV1
      case _: ApplicationService.ApplicationDeletedEvt => ApplicationDeletedEvtManifestV1
      case _: ApplicationState => ApplicationStateManifestV1

      case _ =>
        val errorMsg = s"Can't create manifest for object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    manifest match {
      case UserCreatedEvtManifestV1 => fromUserCreatedEvtV1(bytes)
      case UserUpdatedEvtManifestV1 => fromUserUpdatedEvtV1(bytes)
      case UserDeletedEvtManifestV1 => fromUserDeletedEvtV1(bytes)
      case UserStateManifestV1 => fromUserStateV1(bytes)

      case ApplicationCreatedEvtManifestV1 => fromApplicationCreatedEvtV1(bytes)
      case ApplicationUpdatedEvtManifestV1 => fromApplicationUpdatedEvtV1(bytes)
      case ApplicationDeletedEvtManifestV1 => fromApplicationDeletedEvtV1(bytes)
      case ApplicationStateManifestV1 => fromApplicationStateV1(bytes)

      case _ =>
        val errorMsg = s"Can't deserialize an object using $className manifest [$manifest]"
        throw new IllegalArgumentException(errorMsg)
    }
  }
}