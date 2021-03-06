package annette.core.security.verification

import annette.core.akkaext.actor.CqrsState
import annette.core.security.verification.Verification._
import annette.core.utils.Generator

case class VerificationState(verifications: Map[Verification.Id, VerificationLike]) extends CqrsState with Generator {
  def createVerification(x: VerificationLike): VerificationState = copy(verifications + (x.id -> x))

  def deleteVerification(x: Verification.Id): VerificationState = copy(verifications - x)

  def verificationExists(x: Verification.Id): Boolean = verifications.get(x).isDefined

  def getVerificationById(x: Verification.Id): Option[VerificationLike] = verifications.get(x)

  def verificationsMap: Map[Verification.Id, VerificationLike] = verifications.mapValues {
    case x: Verification => x.copy(code = hide(x.code))
    case x: EmailVerification => x.copy(code = hide(x.code))
  }

  def verificationsIds: Seq[Verification.Id] = verifications.keys.toSeq

  def update = {
    case VerificationCreatedEvt(x) => createVerification(x)
    case VerificationDeletedEvt(x) => deleteVerification(x)
  }
}

object VerificationState {
  def empty = VerificationState(Map.empty)
}