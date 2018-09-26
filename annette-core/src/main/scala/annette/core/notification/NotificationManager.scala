package annette.core.notification

import akka.actor.ActorRef
import akka.util.Timeout
import annette.core.akkaext.actor.ActorId
import javax.inject._

import scala.concurrent.ExecutionContext

@Singleton
class NotificationManager @Inject() (@Named("CoreService") val notificationManagerActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout)
  extends NotificationManagerLike

object NotificationManager {
  type Id = ActorId

  def apply(notificationManagerActor: ActorRef)(implicit c: ExecutionContext, t: Timeout): NotificationManager =
    new NotificationManager(notificationManagerActor)
}