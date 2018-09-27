package annette.core.notification.actor

import annette.core.notification.actor.VerificationActor._
import annette.core.notification._
import annette.core.akkaext.actor.CqrsState
import annette.core.utils.Generator

case class VerificationState(verifications: Map[Verification.Id, Verification]) extends CqrsState with Generator {
  def nonEmpty: Boolean = verifications.nonEmpty
  def create(x: Verification): VerificationState = copy(verifications + (x.id -> x))
  def delete(id: Verification.Id): VerificationState = copy(verifications - id)
  def exists(id: Verification.Id): Boolean = verifications.get(id).isDefined
  def getById(id: Verification.Id): Option[Verification] = verifications.get(id)
  def getAll: Map[Verification.Id, Verification] = verifications.mapValues { x =>
    x.copy(code = hide(x.code))
  }
  def getIds: Seq[Verification.Id] = verifications.keys.toSeq
  def update = {
    case CreatedVerificationEvt(x) => create(x)
    case DeletedVerificationEvt(x) => delete(x)
  }
}

object VerificationState {
  def empty = VerificationState(Map.empty)
}