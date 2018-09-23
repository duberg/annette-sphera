package annette.core.serializer

import java.util.UUID

import annette.core.domain.application.model._
import annette.core.domain.application.{ ApplicationService, ApplicationState }
import annette.core.serializer.proto.application._

trait ApplicationConverters {
  val ApplicationCreatedEvtManifestV1 = "Application.CreatedEvt.v1"
  val ApplicationUpdatedEvtManifestV1 = "Application.UpdatedEvt.v1"
  val ApplicationDeletedEvtManifestV1 = "Application.DeletedEvt.v1"
  val ApplicationStateManifestV1 = "Application.State.v1"

  def toApplicationCreatedEvtBinary(obj: ApplicationService.ApplicationCreatedEvt) = {
    ApplicationCreatedEvtV1(obj.entry).toByteArray
  }

  def toApplicationUpdateEvtBinary(obj: ApplicationService.ApplicationUpdatedEvt): Array[Byte] = {
    ApplicationUpdatedEvtV1(obj.entry).toByteArray
  }

  def toApplicationDeleteEvtBinary(obj: ApplicationService.ApplicationDeletedEvt) = {
    ApplicationDeletedEvtV1(obj.id.toString).toByteArray
  }

  def toApplicationStateBinary(obj: ApplicationState): Array[Byte] = {
    ApplicationStateV1(
      applications = obj.applications.mapValues(fromApplication)).toByteArray
  }

  def fromApplicationCreatedEvtV1(bytes: Array[Byte]): ApplicationService.ApplicationCreatedEvt = {
    val x = ApplicationCreatedEvtV1.parseFrom(bytes)
    ApplicationService.ApplicationCreatedEvt(x.entry)
  }

  def fromApplicationUpdatedEvtV1(bytes: Array[Byte]): ApplicationService.ApplicationUpdatedEvt = {
    val x = ApplicationUpdatedEvtV1.parseFrom(bytes)
    ApplicationService.ApplicationUpdatedEvt(x.entry)
  }

  def fromApplicationDeletedEvtV1(bytes: Array[Byte]): ApplicationService.ApplicationDeletedEvt = {
    val id = ApplicationDeletedEvtV1.parseFrom(bytes).id
    ApplicationService.ApplicationDeletedEvt(id)
  }

  def fromApplicationStateV1(bytes: Array[Byte]): ApplicationState = {
    val p = ApplicationStateV1.parseFrom(bytes)
    ApplicationState(
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

  implicit def toApplicationUpdate(x: ApplicationUpdateV1): ApplicationUpdate = {
    ApplicationUpdate(
      name = x.name,
      code = x.code,
      id = x.id)
  }

  implicit def fromApplicationUpdate(x: ApplicationUpdate): ApplicationUpdateV1 = {
    ApplicationUpdateV1(
      name = x.name,
      code = x.code,
      id = x.id)
  }
}
