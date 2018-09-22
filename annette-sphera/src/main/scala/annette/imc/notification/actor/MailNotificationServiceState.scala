package annette.imc.notification.actor

import annette.core.persistence.Persistence.{ PersistentEvent, PersistentState }
import annette.imc.notification.actor.MailNotificationServiceActor._
import annette.imc.notification.model._

case class MailNotificationServiceState(notifications: Map[Notification.Id, MailNotification]) extends PersistentState[MailNotificationServiceState] {
  def nonEmpty: Boolean = notifications.nonEmpty
  def add(x: MailNotification): MailNotificationServiceState = copy(notifications + (x.id -> x))
  def delete(id: Notification.Id): MailNotificationServiceState = copy(notifications - id)
  def exists(id: Notification.Id): Boolean = notifications.get(id).isDefined
  def getById(id: Notification.Id): Option[MailNotification] = notifications.get(id)
  def copyWithRetry(x: MailNotification, r: Int): MailNotification = x match {
    case x: MailNotification.Password => x.copy(retry = r)
    case x: MailNotification.ToExpertise => x.copy(retry = r)
    case x: MailNotification.ToReview => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): MailNotificationServiceState = {
    val x = notifications(id)
    if (r <= 0) delete(id) else copy(notifications + (x.id -> copyWithRetry(x, r)))
  }
  def updated(event: PersistentEvent): MailNotificationServiceState = event match {
    case AddedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object MailNotificationServiceState {
  def empty = MailNotificationServiceState(Map.empty)
}
