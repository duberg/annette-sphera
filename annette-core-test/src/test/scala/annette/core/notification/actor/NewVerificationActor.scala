package annette.core.notification.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import annette.core.notification.{Verification, VerifyBySmsNotification}
import annette.core.akkaext.actor.ActorId
import annette.core.security.verification.VerificationState
import annette.core.test.PersistenceSpec
import org.scalatest.Assertion

import scala.concurrent.Future

trait NewVerificationActor extends NewSmsNotificationActor { _: PersistenceSpec =>
  import VerificationActor._

  def expectAddNotificationCmd(p: TestProbe, x: Verification): Assertion =
    p.expectMsgType[SmsNotificationActor.CreateNotificationCmd].x shouldBe x.notification

  def createSmsVerification(a: ActorRef, x: Verification): Future[Any] =
    ask(a, CreateVerificationCmd(x))

  def deleteSmsVerification(a: ActorRef, id: Notification.Id): Future[Any] =
    ask(a, DeleteVerificationCmd(id))

  def getSmsVerification(a: ActorRef): Future[Verification] =
    ask(a, GetVerification).mapTo[VerificationOpt].map(_.x.get)

  def getSmsVerifications(a: ActorRef): Future[Map[Notification.Id, Verification]] =
    ask(a, ListVerifications).mapTo[VerificationMap].map(_.x)

  def smsVerify(a: ActorRef, id: Notification.Id, code: String): Future[Any] =
    ask(a, VerifyCmd(id, code))

  def generateSmsVerification(id: Notification.Id = generateUUID): Future[Verification] =
    for (x <- generateSmsNotificationVerification()) yield {
      SmsVerification(
        id = id,
        notification = x)
    }

  def newSmsVerificationActor(id: ActorId = generateActorId, state: VerificationState = VerificationState.empty): Future[(TestProbe, ActorRef)] = Future {
    val smsNotificationServiceActorProbe = newTestProbe
    val a = system.actorOf(VerificationActor.props(id, smsNotificationServiceActorProbe.ref, state), id.name)
    (smsNotificationServiceActorProbe, a)
  }
}