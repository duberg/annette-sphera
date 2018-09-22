package annette.imc.notification.actor

import annette.core.persistence.Persistence.{ PersistentEvent, PersistentState }
import annette.imc.notification.actor.SmsNotificationServiceActor._
import annette.imc.notification.model._

case class SmsNotificationServiceState(notifications: Map[Notification.Id, SmsNotification]) extends PersistentState[SmsNotificationServiceState] {
  def nonEmpty: Boolean = notifications.nonEmpty
  def add(x: SmsNotification): SmsNotificationServiceState = copy(notifications + (x.id -> x))
  def delete(id: Notification.Id): SmsNotificationServiceState = copy(notifications - id)
  def exists(id: Notification.Id): Boolean = notifications.get(id).isDefined
  def getById(id: Notification.Id): Option[SmsNotification] = notifications.get(id)
  def ids: Seq[Notification.Id] = notifications.keys.toSeq
  def copyWithRetry(x: SmsNotification, r: Int): SmsNotification = x match {
    case x: SmsNotification.Password => x.copy(retry = r)
    case x: SmsNotification.Verification => x.copy(retry = r)
    case x: SmsNotification.ToExpertise => x.copy(retry = r)
    case x: SmsNotification.ToReview => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): SmsNotificationServiceState = {
    val x = notifications(id)
    if (r <= 0) delete(id) else copy(notifications + (x.id -> copyWithRetry(x, r)))
  }
  def updated(event: PersistentEvent): SmsNotificationServiceState = event match {
    case AddedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object SmsNotificationServiceState {
  def empty = SmsNotificationServiceState(Map.empty)
}
