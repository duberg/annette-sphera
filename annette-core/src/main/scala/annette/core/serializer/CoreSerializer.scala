package annette.core.serializer

import akka.serialization.SerializerWithStringManifest
import annette.core.domain.tenancy.{ UserService, UserState }

class CoreSerializer extends SerializerWithStringManifest
  with UserConverters {

  private val className = Option(this.getClass.getName).getOrElse("<undefined>")

  override def identifier: Int = 20170922

  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case obj: UserService.UserCreatedEvt => toUserCreatedEvtBinary(obj)
      case obj: UserService.UserUpdatedEvt => toUserUpdateEvtBinary(obj)
      case obj: UserService.UserDeletedEvt => toUserDeleteEvtBinary(obj)
      case obj: UserState => toUserStateBinary(obj)

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
      case _: UserState => UserStateManifestV1

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

      case _ =>
        val errorMsg = s"Can't deserialize an object using $className manifest [$manifest]"
        throw new IllegalArgumentException(errorMsg)
    }
  }
}