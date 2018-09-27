package annette.core.notification.actor

import annette.core.notification.actor.SmsNotificationActor._
import annette.core.notification._
import annette.core.akkaext.actor.CqrsState
import com.sun.xml.internal.ws.resources.SenderMessages

case class SmsNotificationState(v: Map[Notification.Id, SmsNotificationLike]) extends CqrsState {
  def nonEmpty: Boolean = v.nonEmpty
  def add(x: SmsNotificationLike): SmsNotificationState = copy(v + (x.id -> x))
  def delete(id: Notification.Id): SmsNotificationState = copy(v - id)
  def exists(id: Notification.Id): Boolean = v.get(id).isDefined
  def getById(id: Notification.Id): Option[SmsNotificationLike] = v.get(id)
  def ids: Seq[Notification.Id] = v.keys.toSeq
  def copyWithRetry(x: SmsNotificationLike, r: Int): SmsNotificationLike = x match {
    case x: SendPasswordToPhoneNotification => x.copy(retry = r)
    case x: VerifyBySmsNotification => x.copy(retry = r)
    case x: SmsNotification => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): SmsNotificationState = {
    val x = v(id)
    if (r <= 0) delete(id) else copy(v + (x.id -> copyWithRetry(x, r)))
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
