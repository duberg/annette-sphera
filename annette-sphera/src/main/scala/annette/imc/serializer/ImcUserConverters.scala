
package annette.imc.serializer

import java.util.UUID

import annette.imc.serializer.proto.imcuser._
import annette.imc.user.model.ImcUser
import annette.imc.user.{ ImcUserActor, ImcUserState }

trait ImcUserConverters {

  val ImcUserCreatedEvtManifestV1 = "ImcUser.CreatedEvt.v1"
  val ImcUserCreatedEvtManifestV2 = "ImcUser.CreatedEvt.v2"
  val ImcUserUpdatedEvtManifestV1 = "ImcUser.UpdatedEvt.v1"
  val ImcUserUpdatedEvtManifestV2 = "ImcUser.UpdatedEvt.v2"
  val ImcUserDeletedEvtManifestV1 = "ImcUser.DeletedEvt.v1"
  val ImcUserStateManifestV1 = "ImcUser.State.v1"
  val ImcUserStateManifestV2 = "ImcUser.State.v2"

  def toImcUserCreatedEvtBinary(obj: ImcUserActor.CreatedEvt) = {
    ImcUserCreatedEvtV2(fromImcUser(obj.entry))
      .toByteArray
  }

  def toImcUserUpdateEvtBinary(obj: ImcUserActor.UpdatedEvt): Array[Byte] = {
    ImcUserUpdatedEvtV2(fromImcUser(obj.entry))
      .toByteArray
  }

  def toImcUserDeleteEvtBinary(obj: ImcUserActor.DeletedEvt) = {
    ImcUserDeletedEvtV1(obj.id.toString)
      .toByteArray
  }

  def toImcUserStateBinary(obj: ImcUserState): Array[Byte] = {
    val storage = obj.storage.map {
      case (id, imcUser) =>
        val d: ImcUserV2 = fromImcUser(imcUser)
        id.toString -> d
    }
    ImcUserStateV2(storage)
      .toByteArray
  }

  def fromImcUserCreatedEvtV1(bytes: Array[Byte]): ImcUserActor.CreatedEvt = {
    val d = ImcUserCreatedEvtV1.parseFrom(bytes).imcUser
    ImcUserActor.CreatedEvt(toImcUser(d))
  }

  def fromImcUserCreatedEvtV2(bytes: Array[Byte]): ImcUserActor.CreatedEvt = {
    val d = ImcUserCreatedEvtV2.parseFrom(bytes).imcUser
    ImcUserActor.CreatedEvt(toImcUser(d))
  }

  def fromImcUserUpdatedEvtV1(bytes: Array[Byte]): ImcUserActor.UpdatedEvt = {
    val d = ImcUserUpdatedEvtV1.parseFrom(bytes).imcUser
    ImcUserActor.UpdatedEvt(toImcUser(d))
  }

  def fromImcUserUpdatedEvtV2(bytes: Array[Byte]): ImcUserActor.UpdatedEvt = {
    val d = ImcUserUpdatedEvtV2.parseFrom(bytes).imcUser
    ImcUserActor.UpdatedEvt(toImcUser(d))
  }

  def fromImcUserDeletedEvtV1(bytes: Array[Byte]): ImcUserActor.DeletedEvt = {
    val id = UUID.fromString(ImcUserDeletedEvtV1.parseFrom(bytes).id)
    ImcUserActor.DeletedEvt(id)
  }

  def fromImcUserStateV1(bytes: Array[Byte]): ImcUserState = {
    val storage = ImcUserStateV1.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toImcUser(d)
      }
    ImcUserState(storage)
  }

  def fromImcUserStateV2(bytes: Array[Byte]): ImcUserState = {
    val storage = ImcUserStateV2.parseFrom(bytes).storage
      .map {
        case (id, d) =>
          UUID.fromString(id) -> toImcUser(d)
      }
    ImcUserState(storage)
  }

  def toImcUser(d: ImcUserV1): ImcUser =
    ImcUser(
      id = UUID.fromString(d.id),
      sphere = d.sphere,
      company = d.company,
      position = d.position,
      rank = d.rank,
      postponed = d.postponed)

  def toImcUser(d: ImcUserV2): ImcUser =
    ImcUser(
      id = UUID.fromString(d.id),
      sphere = d.sphere,
      company = d.company,
      position = d.position,
      rank = d.rank,
      postponed = d.postponed,
      otherTel = d.otherTel,
      otherMail = d.otherMail)

  def fromImcUser(d: ImcUser): ImcUserV2 = {
    ImcUserV2(
      id = d.id.toString,
      sphere = d.sphere,
      company = d.company,
      position = d.position,
      rank = d.rank,
      postponed = d.postponed,
      otherTel = d.otherTel,
      otherMail = d.otherMail)
  }

}
