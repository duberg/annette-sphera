package annette.core.notification

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.notification.actor.NotificationManagerActor
import annette.core.security.verification.VerificationBus
import annette.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.Future

trait NewNotificationManager { _: PersistenceSpec with TestKit =>
  lazy val config: Config = ConfigFactory.load()

  def newNotificationManagerActor(id: String = generateString()): Future[ActorRef] = Future {
    system.actorOf(
      props = NotificationManagerActor.props(
        config = config,
        verificationBus = new VerificationBus),
      name = id)
  }

  def newNotificationManager(id: String = generateString()): Future[NotificationManager] =
    newNotificationManagerActor()
      .map(NotificationManager(_))
}
