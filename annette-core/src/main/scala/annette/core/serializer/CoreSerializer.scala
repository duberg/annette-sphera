package annette.core.serializer

import akka.serialization.SerializerWithStringManifest
import annette.core.domain.tenancy.UserService
import annette.core.domain.application.model._
import annette.core.domain.application.{ ApplicationService, ApplicationState }
import annette.core.domain.tenancy.actor.UsersState

class CoreSerializer extends SerializerWithStringManifest
  with UserConverters
  with ApplicationConverters {

  private val className = Option(this.getClass.getName).getOrElse("<undefined>")

  override def identifier: Int = 20170922

  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case obj: UserService.CreatedUserEvt => toCreatedUserEvtBinary(obj)
      case obj: UserService.UpdatedUserEvt => toUpdateUserEvtBinary(obj)
      case obj: UserService.DeletedUserEvt => toDeleteUserEvtBinary(obj)
      case obj: UsersState => toUserStatesBinary(obj)

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
      case _: UserService.CreatedUserEvt => CreatedUserEvtManifestV1
      case _: UserService.UpdatedUserEvt => UpdatedUserEvtManifestV1
      case _: UserService.DeletedUserEvt => DeletedUserEvtManifestV1
      case _: UsersState => UsersStateManifestV1

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
      case CreatedUserEvtManifestV1 => fromCreatedUserEvtV1(bytes)
      case UpdatedUserEvtManifestV1 => fromUpdatedUserEvtV1(bytes)
      case DeletedUserEvtManifestV1 => fromDeletedUserEvtV1(bytes)
      case UsersStateManifestV1 => fromUsersStateV1(bytes)

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