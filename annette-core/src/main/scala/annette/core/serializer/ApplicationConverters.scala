package annette.core.serializer

import java.util.UUID

import annette.core.domain.application._
import annette.core.domain.application.Application._
import annette.core.domain.application.{ ApplicationManager, ApplicationManagerState }
import annette.core.serializer.proto.application._

trait ApplicationConverters {
  val ApplicationCreatedEvtManifestV1 = "Application.CreatedEvt.v1"
  val ApplicationUpdatedEvtManifestV1 = "Application.UpdatedEvt.v1"
  val ApplicationDeletedEvtManifestV1 = "Application.DeletedEvt.v1"
  val ApplicationStateManifestV1 = "Application.State.v1"

  def toApplicationCreatedEvtBinary(obj: ApplicationCreatedEvt) = {
    ApplicationCreatedEvtV1(obj.x).toByteArray
  }

  def toApplicationUpdateEvtBinary(obj: ApplicationUpdatedEvt): Array[Byte] = {
    ApplicationUpdatedEvtV1(obj.x).toByteArray
  }

  def toApplicationDeleteEvtBinary(obj: ApplicationDeletedEvt) = {
    ApplicationDeletedEvtV1(obj.x.toString).toByteArray
  }

  def toApplicationStateBinary(obj: ApplicationManagerState): Array[Byte] = {
    ApplicationStateV1(
      applications = obj.applications.mapValues(fromApplication)).toByteArray
  }

  def fromApplicationCreatedEvt(bytes: Array[Byte]): ApplicationCreatedEvt = {
    val x = ApplicationCreatedEvtV1.parseFrom(bytes)
    ApplicationCreatedEvt(x.x)
  }

  def fromApplicationUpdatedEvt(bytes: Array[Byte]): ApplicationUpdatedEvt = {
    val x = ApplicationUpdatedEvtV1.parseFrom(bytes)
    ApplicationUpdatedEvt(x.x)
  }

  def fromApplicationDeletedEvt(bytes: Array[Byte]): Application.ApplicationDeletedEvt = {
    val id = ApplicationDeletedEvtV1.parseFrom(bytes).x
    ApplicationDeletedEvt(id)
  }

  def fromApplicationStateV1(bytes: Array[Byte]): ApplicationManagerState = {
    val p = ApplicationStateV1.parseFrom(bytes)
    ApplicationManagerState(
      applications = p.applications.mapValues(toApplication))
  }

  implicit def toApplication(x: ApplicationV1): Application =
    Application(
      name = x.name,
      code = x.code,
      id = x.id)

  implicit def fromApplication(x: Application): ApplicationV1 = {
    ApplicationV1(
      name = x.name,
      code = x.code,
      id = x.id)
  }

  implicit def toUpdateApplication(x: UpdateApplicationV1): UpdateApplication = {
    UpdateApplication(
      name = x.name,
      code = x.code,
      id = x.id)
  }

  implicit def fromUpdateApplication(x: UpdateApplication): UpdateApplicationV1 = {
    UpdateApplicationV1(
      name = x.name,
      code = x.code,
      id = x.id)
  }
}
