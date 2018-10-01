//package annette.imc.notification.actor
//
//import akka.actor.ActorRef
//import akka.pattern.ask
//import akka.testkit.TestProbe
//import annette.core.test.PersistenceSpec
//import annette.imc.ApsActor
//import annette.imc.model.{ApStatus, UpdateBulletin}
//import annette.imc.notification.actor.SmsVerificationServiceActor._
//import annette.imc.notification.model._
//import org.scalatest.Assertion
//
//import scala.concurrent.Future
//
//trait NewSmsVerificationService { _: PersistenceSpec =>
//  def expectAddNotificationCmd(p: TestProbe, x: SmsVerification): Assertion =
//    p.expectMsgType[SmsNotificationServiceActor.AddNotificationCmd].x shouldBe SmsNotification.Verification(
//      id = x.id,
//      phone = x.phone,
//      code = x.code,
//      language = "RU")
//
//  def expectChangeStatusCmd(p: TestProbe): Assertion = {
//    p.expectMsgType[ApsActor.ChangeStatusCmd]
//    succeed
//  }
//
//  def expectUpdateBulletinCmd(p: TestProbe): Assertion = {
//    p.expectMsgType[ApsActor.UpdateBulletinCmd]
//    succeed
//  }
//
//  def addSmsVerification(a: ActorRef, x: SmsVerification): Future[Any] =
//    ask(a, AddVerificationCmd(x))
//
//  def deleteSmsVerification(a: ActorRef, id: Verification.Id): Future[Any] =
//    ask(a, DeleteVerificationCmd(id))
//
//  def getSmsVerification(a: ActorRef): Future[SmsVerification] =
//    ask(a, GetVerification).mapTo[VerificationOpt].map(_.x.get)
//
//  def getSmsVerifications(a: ActorRef): Future[Map[Verification.Id, SmsVerification]] =
//    ask(a, GetVerifications).mapTo[VerificationsRes].map(_.x)
//
//  def smsVerify(a: ActorRef, id: Verification.Id, code: String): Future[Any] =
//    ask(a, VerifyCmd(id, code))
//
//  def generateSmsVerificationStatus(id: Verification.Id = generateUUID): Future[SmsVerification.Status] = Future {
//    SmsVerification.Status(
//      id = id,
//      code = generatePinString,
//      phone = generateId,
//      language = "RU")
//  }
//
//  def generateSmsVerificationVoted(id: Verification.Id = generateUUID): Future[SmsVerification.Voted] = Future {
//    SmsVerification.Voted(
//      id = id,
//      code = generatePinString,
//      phone = generateId,
//      apId = generateUUID,
//      bulletin = UpdateBulletin(
//        expertId = generateUUID),
//      language = "RU")
//  }
//
//  def newSmsVerificationService(id: String = generateId, state: SmsVerificationServiceState = SmsVerificationServiceState.empty): Future[(TestProbe, TestProbe, ActorRef)] = Future {
//    val apsActorProbe = newTestProbe
//    val smsNotificationServiceActorProbe = newTestProbe
//    (apsActorProbe, smsNotificationServiceActorProbe, system.actorOf(SmsVerificationServiceActor.props(id, apsActorProbe.ref, smsNotificationServiceActorProbe.ref, state), id))
//  }
//}