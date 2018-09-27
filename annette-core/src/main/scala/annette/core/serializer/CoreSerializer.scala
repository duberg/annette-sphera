package annette.core.serializer

import akka.serialization.SerializerWithStringManifest
import annette.core.domain.tenancy.UserManager
import annette.core.domain.application._
import annette.core.domain.application.{ ApplicationManager, ApplicationManagerState }
import annette.core.domain.tenancy.actor.UsersState
import annette.core.security.verification.{ Verification, VerificationState }

class CoreSerializer extends SerializerWithStringManifest
  with UserConverters
  with ApplicationConverters
  with VerificationConverters {

  private val className = Option(this.getClass.getName).getOrElse("<undefined>")

  override def identifier: Int = 20170922

  override def toBinary(o: AnyRef): Array[Byte] = {
    o match {
      case obj: UserManager.CreatedUserEvt => toCreatedUserEvtBinary(obj)
      case obj: UserManager.UpdatedUserEvt => toUpdateUserEvtBinary(obj)
      case obj: UserManager.DeletedUserEvt => toDeleteUserEvtBinary(obj)
      case obj: UsersState => toUserStatesBinary(obj)

      case obj: Application.ApplicationCreatedEvt => toApplicationCreatedEvtBinary(obj)
      case obj: Application.ApplicationUpdatedEvt => toApplicationUpdateEvtBinary(obj)
      case obj: Application.ApplicationDeletedEvt => toApplicationDeleteEvtBinary(obj)
      case obj: ApplicationManagerState => toApplicationStateBinary(obj)

      case obj: Verification.VerificationCreatedEvt => toVerificationCreatedEvtBinary(obj)
      case obj: Verification.VerificationDeletedEvt => toVerificationDeletedEvtBinary(obj)
      case obj: VerificationState => toVerificationStateBinary(obj)

      case _ =>
        val errorMsg = s"Can't serialize an object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def manifest(o: AnyRef): String = {
    o match {
      case _: UserManager.CreatedUserEvt => CreatedUserEvtManifestV1
      case _: UserManager.UpdatedUserEvt => UpdatedUserEvtManifestV1
      case _: UserManager.DeletedUserEvt => DeletedUserEvtManifestV1
      case _: UsersState => UsersStateManifestV1

      case _: Application.ApplicationCreatedEvt => ApplicationCreatedEvtManifestV1
      case _: Application.ApplicationUpdatedEvt => ApplicationUpdatedEvtManifestV1
      case _: Application.ApplicationDeletedEvt => ApplicationDeletedEvtManifestV1
      case _: ApplicationManagerState => ApplicationStateManifestV1

      case _: Verification.VerificationCreatedEvt => VerificationCreatedEvtManifestV1
      case _: Verification.VerificationDeletedEvt => VerificationDeletedEvtManifestV1
      case _: VerificationState => VerificationStateManifestV1

      case _ =>
        val errorMsg = s"Can't create manifest for object using $className [${o.toString}]"
        throw new IllegalArgumentException(errorMsg)
    }
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    manifest match {
      case CreatedUserEvtManifestV1 => fromCreatedUserEvt(bytes)
      case UpdatedUserEvtManifestV1 => fromUpdatedUserEvt(bytes)
      case DeletedUserEvtManifestV1 => fromDeletedUserEvt(bytes)
      case UsersStateManifestV1 => fromUsersStateV1(bytes)

      case ApplicationCreatedEvtManifestV1 => fromApplicationCreatedEvt(bytes)
      case ApplicationUpdatedEvtManifestV1 => fromApplicationUpdatedEvt(bytes)
      case ApplicationDeletedEvtManifestV1 => fromApplicationDeletedEvt(bytes)
      case ApplicationStateManifestV1 => fromApplicationStateV1(bytes)

      case VerificationCreatedEvtManifestV1 => fromVerificationCreatedEvt(bytes)
      case VerificationDeletedEvtManifestV1 => fromVerificationDeletedEvt(bytes)
      case VerificationStateManifestV1 => fromVerificationState(bytes)

      case _ =>
        val errorMsg = s"Can't deserialize an object using $className manifest [$manifest]"
        throw new IllegalArgumentException(errorMsg)
    }
  }
}