package annette.core.notification.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.notification._
import annette.core.notification.actor.SmsNotificationActor._

case class SmsNotificationState(notifications: Map[Notification.Id, SmsNotificationLike]) extends CqrsState {
  def nonEmpty: Boolean = notifications.nonEmpty
  def add(x: SmsNotificationLike): SmsNotificationState = copy(notifications + (x.id -> x))
  def delete(id: Notification.Id): SmsNotificationState = copy(notifications - id)
  def exists(id: Notification.Id): Boolean = notifications.get(id).isDefined
  def getById(id: Notification.Id): Option[SmsNotificationLike] = notifications.get(id)
  def ids: Seq[Notification.Id] = notifications.keys.toSeq
  def copyWithRetry(x: SmsNotificationLike, r: Int): SmsNotificationLike = x match {
    case x: SendPasswordToPhoneNotification => x.copy(retry = r)
    case x: VerifyBySmsNotification => x.copy(retry = r)
    case x: SmsNotification => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): SmsNotificationState = {
    val x = notifications(id)
    if (r <= 0) delete(id) else copy(notifications + (x.id -> copyWithRetry(x, r)))
  }
  def update = {
    case CreatedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object SmsNotificationState {
  def empty = SmsNotificationState(Map.empty)
}
