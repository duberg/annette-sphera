package annette.core.notification.actor

import akka.actor.{ActorRef, Props, Terminated}
import annette.core.notification.actor.WebSocketNotificationActor._
import annette.core.notification.{CreateWebSocketNotification, NotificationManager, WebSocketNotificationLike}
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._
import annette.core.domain.tenancy.model.User

private class WebSocketNotificationActor(val id: NotificationManager.Id, val initState: WebSocketNotificationState)
  extends CqrsPersistentActor[WebSocketNotificationState] {

  def connect(x: User.Id, y: ActorRef): Unit = {
    // addSubscriber(self, x, y)
    context.watch(y)
  }
  def disconnect(x: ActorRef): Unit = {
    ???
    //removeSubscriber(self, x)
  }

  def notify(x: CreateWebSocketNotification): Unit = {
    // subscribersMap
    // .filter(x.userIds contains _._1)
    // .foreach(_._2 ! x)
    sender() ! Done
  }

  def behavior(state: State): Receive = {
    case ConnectCmd(x, y) => connect(x, y)
    case NotifyCmd(x) => notify(x)
    case Terminated(x) => disconnect(x)
  }
}

object WebSocketNotificationActor {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class ConnectCmd(x: User.Id, y: ActorRef) extends Query
  case class NotifyCmd(x: CreateWebSocketNotification) extends Query

  case object Done extends CqrsResponse

  def props(
    id: NotificationManager.Id,
    state: WebSocketNotificationState = WebSocketNotificationState.empty): Props =
    Props(new WebSocketNotificationActor(id, state))
}
