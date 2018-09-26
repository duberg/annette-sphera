package annette.core.notification.actor

import annette.core.akkaext.actor.CqrsState

case class WebSocketNotificationState() extends CqrsState {
  def update = PartialFunction.empty
}

object WebSocketNotificationState {
  def empty = WebSocketNotificationState()
}
