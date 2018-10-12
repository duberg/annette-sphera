package annette.core.notification.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.notification._
import annette.core.notification.actor.EmailNotificationActor._

case class EmailNotificationState(notifications: Map[Notification.Id, EmailNotificationLike]) extends CqrsState {
  def nonEmpty: Boolean = notifications.nonEmpty
  def add(x: EmailNotificationLike): EmailNotificationState = copy(notifications + (x.id -> x))
  def delete(id: Notification.Id): EmailNotificationState = copy(notifications - id)
  def exists(id: Notification.Id): Boolean = notifications.get(id).isDefined
  def getById(id: Notification.Id): Option[EmailNotificationLike] = notifications.get(id)
  def copyWithRetry(x: EmailNotificationLike, r: Int): EmailNotificationLike = x match {
    case x: SendPasswordToEmailNotification => x.copy(retry = r)
    case x: VerifyByEmailNotification => x.copy(retry = r)
    case x: EmailNotification => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): EmailNotificationState = {
    val x = notifications(id)
    if (r <= 0) delete(id) else copy(notifications + (x.id -> copyWithRetry(x, r)))
  }
  def update = {
    case CreatedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object EmailNotificationState {
  def empty = EmailNotificationState(Map.empty)
}
