package annette.core.notification.actor

import annette.core.notification.actor.EmailNotificationActor._
import annette.core.notification._
import annette.core.akkaext.actor.CqrsState

case class EmailNotificationState(v: Map[Notification.Id, EmailNotificationLike]) extends CqrsState {
  def nonEmpty: Boolean = v.nonEmpty
  def add(x: EmailNotificationLike): EmailNotificationState = copy(v + (x.id -> x))
  def delete(id: Notification.Id): EmailNotificationState = copy(v - id)
  def exists(id: Notification.Id): Boolean = v.get(id).isDefined
  def getById(id: Notification.Id): Option[EmailNotificationLike] = v.get(id)
  def copyWithRetry(x: EmailNotificationLike, r: Int): EmailNotificationLike = x match {
    case x: SendPasswordToEmailNotification => x.copy(retry = r)
    case x: VerifyByEmailNotification => x.copy(retry = r)
    case x: EmailNotification => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): EmailNotificationState = {
    val x = v(id)
    if (r <= 0) delete(id) else copy(v + (x.id -> copyWithRetry(x, r)))
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
