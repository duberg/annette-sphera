package annette.core.notification

import akka.actor.ActorRef
import akka.util.Timeout
import javax.inject._

import scala.concurrent.ExecutionContext

@Singleton
class NotificationService @Inject() (@Named("CoreService") val notificationManagerActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout)
  extends NotificationManagerLike

object NotificationService {
  def apply(notificationManagerActor: ActorRef)(implicit c: ExecutionContext, t: Timeout): NotificationService =
    new NotificationService(notificationManagerActor)
}