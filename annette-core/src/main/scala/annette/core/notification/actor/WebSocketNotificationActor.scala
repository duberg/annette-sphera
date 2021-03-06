package annette.core.notification.actor

import akka.actor.{ ActorRef, Props, Terminated }
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._
import annette.core.domain.tenancy.model.User
import annette.core.notification.CreateWebSocketNotificationLike
import annette.core.notification.actor.WebSocketNotificationActor._

private class WebSocketNotificationActor(val initState: WebSocketNotificationState = WebSocketNotificationState.empty) extends CqrsPersistentActor[WebSocketNotificationState] {
  def connect(x: User.Id, y: ActorRef): Unit = {
    // addSubscriber(self, x, y)
    context.watch(y)
  }
  def disconnect(x: ActorRef): Unit = {
    ???
    //removeSubscriber(self, x)
  }

  def notify(x: CreateWebSocketNotificationLike): Unit = {
    // subscribersMap
    // .filter(x.userIds contains _._1)
    // .foreach(_._2 ! x)
    sender() ! Done
  }

  def behavior(state: WebSocketNotificationState): Receive = {
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
  case class NotifyCmd(x: CreateWebSocketNotificationLike) extends Query

  case object Done extends CqrsResponse

  def props: Props = Props(new WebSocketNotificationActor)
}
