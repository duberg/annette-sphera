package annette.core.notification

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.notification.actor.NotificationManagerActor
import annette.core.akkaext.actor.ActorId
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.Future

trait NewNotificationManager { _: PersistenceSpec with TestKit =>
  lazy val config: Config = ConfigFactory.load()

  def generateNotificationManagerId = ActorId(s"notificationManager-$generateInt")

  def newNotificationManagerActor(id: NotificationManager.Id = generateNotificationManagerId): Future[ActorRef] = Future {
    system.actorOf(
      props = NotificationManagerActor.props(
        id = id,
        config = config),
      name = id.name)
  }

  def newNotificationManager(id: NotificationManager.Id = generateNotificationManagerId): Future[NotificationManager] =
    newNotificationManagerActor()
      .map(NotificationManager(_))
}
