package annette.core.serializer

import java.util.UUID

import annette.core.security.verification.Verification._
import annette.core.security.verification._
import Implicits._
import annette.core.serializer.proto.verification._

trait VerificationConverters {
  val VerificationStateManifestV1 = "VerificationState.v1"
  val VerificationCreatedEvtManifestV1 = "VerificationCreatedEvt.v1"
  val VerificationDeletedEvtManifestV1 = "VerificationDeletedEvt.v1"

  def toVerificationCreatedEvtBinary(obj: VerificationCreatedEvt): Array[Byte] =
    VerificationCreatedEvtV1(toVerification(obj.x))
      .toByteArray

  def toVerificationDeletedEvtBinary(obj: VerificationDeletedEvt): Array[Byte] =
    VerificationDeletedEvtV1(obj.x)
      .toByteArray

  def toVerificationStateBinary(obj: VerificationState): Array[Byte] = {
    val n = obj.verifications.map { case (x, y) => x.toString -> toVerification(y) }
    VerificationStateV1(n).toByteArray
  }

  def fromVerificationCreatedEvt(bytes: Array[Byte]): VerificationCreatedEvt = {
    val x = VerificationCreatedEvtV1.parseFrom(bytes).x
    VerificationCreatedEvt(fromVerification(x))
  }

  def fromVerificationDeletedEvt(bytes: Array[Byte]): VerificationDeletedEvt = {
    val x = UUID.fromString(VerificationDeletedEvtV1.parseFrom(bytes).x)
    VerificationDeletedEvt(x)
  }

  def fromVerificationState(bytes: Array[Byte]): VerificationState = {
    val x = VerificationStateV1.parseFrom(bytes).verifications.map {
      case (a, b) => (UUID.fromString(a), fromVerification(b))
    }
    VerificationState(x)
  }

  implicit def toVerification(x: Verification): VerificationV1 = {
    VerificationV1(
      id = x.id,
      code = x.code,
      duration = x.duration)
  }

  implicit def fromVerification(x: VerificationV1): Verification = {
    Verification(
      id = x.id,
      code = x.code,
      duration = x.duration)
  }
}