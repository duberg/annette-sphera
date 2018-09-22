package annette.imc.notification.actor

import annette.core.persistence.Persistence.{ PersistentEvent, PersistentState }
import annette.core.utils.Generator
import annette.imc.notification.actor.SmsVerificationServiceActor._
import annette.imc.notification.model._

case class SmsVerificationServiceState(v: Map[Verification.Id, SmsVerification]) extends PersistentState[SmsVerificationServiceState]
  with Generator {
  def nonEmpty: Boolean = v.nonEmpty
  def add(x: SmsVerification): SmsVerificationServiceState = copy(v + (x.id -> x))
  def delete(id: Verification.Id): SmsVerificationServiceState = copy(v - id)
  def exists(id: Verification.Id): Boolean = v.get(id).isDefined
  def getById(id: Verification.Id): Option[SmsVerification] = v.get(id)
  def getAll: Map[Verification.Id, SmsVerification] = v.mapValues {
    case x: SmsVerification.Status => x.copy(code = hide(x.code))
    case x: SmsVerification.Voted => x.copy(code = hide(x.code))
  }
  def getIds: Seq[Verification.Id] = v.keys.toSeq
  def updated(event: PersistentEvent): SmsVerificationServiceState = event match {
    case AddedVerificationEvt(x) => add(x)
    case DeletedVerificationEvt(x) => delete(x)
  }
}

object SmsVerificationServiceState {
  def empty = SmsVerificationServiceState(Map.empty)
}