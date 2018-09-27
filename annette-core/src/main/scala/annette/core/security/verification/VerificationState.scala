package annette.core.security.verification

import annette.core.akkaext.actor.CqrsState
import annette.core.notification._
import annette.core.security.verification.Verification._
import annette.core.utils.Generator

case class VerificationState(verifications: Map[Verification.Id, Verification]) extends CqrsState with Generator {
  def createVerification(x: Verification): VerificationState = copy(verifications + (x.id -> x))

  def deleteVerification(x: Verification.Id): VerificationState = copy(verifications - x)

  def verificationExists(x: Verification.Id): Boolean = verifications.get(x).isDefined

  def getVerificationById(x: Verification.Id): Option[Verification] = verifications.get(x)

  def verificationsMap: Map[Verification.Id, Verification] = verifications.mapValues { x =>
    x.copy(code = hide(x.code))
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