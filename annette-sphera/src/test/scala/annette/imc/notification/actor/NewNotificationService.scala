//package annette.imc.notification.actor
//
//import akka.actor.ActorRef
//import akka.testkit.TestProbe
//import annette.core.test.PersistenceSpec
//import annette.imc.notification.NotificationService
//import com.typesafe.config.{ Config, ConfigFactory }
//
//import scala.concurrent.Future
//
//trait NewNotificationService { _: PersistenceSpec =>
//  lazy val config: Config = ConfigFactory.load()
//
//  def newNotificationService: Future[ActorRef] = Future {
//    val p = newTestProbeRef
//    val id = generateId
//    system.actorOf(NotificationServiceActor.props(id, p, config), id)
//  }
//
//  def newNotificationServiceWrapper: Future[(TestProbe, NotificationService)] = Future {
//    val p = newTestProbe
//    (p, NotificationService(p.ref, config))
//  }
//}
//
