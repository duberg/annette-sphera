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
    VerificationCreatedEvtV1(toVerificationLike(obj.x))
      .toByteArray

  def toVerificationDeletedEvtBinary(obj: VerificationDeletedEvt): Array[Byte] =
    VerificationDeletedEvtV1(obj.x)
      .toByteArray

  def toVerificationStateBinary(obj: VerificationState): Array[Byte] = {
    val n = obj.verifications.map { case (x, y) => x.toString -> toVerificationLike(y) }
    VerificationStateV1(n).toByteArray
  }

  def fromVerificationCreatedEvt(bytes: Array[Byte]): VerificationCreatedEvt = {
    val x = VerificationCreatedEvtV1.parseFrom(bytes).x
    VerificationCreatedEvt(fromVerificationLike(x))
  }

  def fromVerificationDeletedEvt(bytes: Array[Byte]): VerificationDeletedEvt = {
    val x = UUID.fromString(VerificationDeletedEvtV1.parseFrom(bytes).x)
    VerificationDeletedEvt(x)
  }

  def fromVerificationState(bytes: Array[Byte]): VerificationState = {
    val x = VerificationStateV1.parseFrom(bytes).verifications.map {
      case (a, b) => (UUID.fromString(a), fromVerificationLike(b))
    }
    VerificationState(x)
  }

  implicit def toVerificationLike(x: VerificationLike): VerificationLikeV1 = x match {
    case y: Verification => VerificationLikeV1.defaultInstance.withOpt1(
      VerificationV1(
        id = y.id,
        code = y.code,
        duration = y.duration))
    case y: EmailVerification => VerificationLikeV1.defaultInstance.withOpt2(
      EmailVerificationV1(
        id = y.id,
        code = y.code,
        email = y.email,
        duration = y.duration))
  }

  implicit def fromVerificationLike(x: VerificationLikeV1): VerificationLike = {
    val opt1 = x.verificationLike.opt1
    val opt2 = x.verificationLike.opt2

    Seq(opt1, opt2).flatten.head match {
      case y: VerificationV1 => Verification(
        id = y.id,
        code = y.code,
        duration = y.duration)
      case y: EmailVerificationV1 => EmailVerification(
        id = y.id,
        code = y.code,
        email = y.email,
        duration = y.duration)
    }
  }
}