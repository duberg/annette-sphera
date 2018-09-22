package annette.imc.notification.actor

import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import annette.core.persistence.Persistence._
import annette.imc.ApsActor
import annette.imc.notification.model.{ SmsVerification, _ }

import scala.concurrent.ExecutionContext

private class SmsVerificationServiceActor(
  val id: String,
  val apsActor: ActorRef,
  val smsNotificationServiceActor: ActorRef,
  val initState: SmsVerificationServiceState)(implicit val executor: ExecutionContext, val timeout: Timeout) extends PersistentStateActor[SmsVerificationServiceState] {
  import SmsVerificationServiceActor._

  def addVerification(state: SmsVerificationServiceState, x: SmsVerification): Unit = {
    if (state.exists(x.id)) sender ! VerificationAlreadyExists else {
      persist(AddedVerificationEvt(x)) { event =>
        changeState(state.updated(event))
        smsNotificationServiceActor ! SmsNotificationServiceActor.AddNotificationCmd(SmsNotification.Verification(
          id = x.id,
          phone = x.phone,
          code = x.code,
          language = x.language))
        context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
        sender ! Done
      }
    }
  }

  def deleteVerification(state: SmsVerificationServiceState, id: Verification.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedVerificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! VerificationNotFound
  }

  def findVerification(state: SmsVerificationServiceState, id: Verification.Id): Unit =
    sender() ! VerificationOpt(state.getById(id))

  def findVerifications(state: SmsVerificationServiceState): Unit =
    sender() ! VerificationsRes(state.getAll)

  def verify(state: SmsVerificationServiceState, id: Verification.Id, code: String): Unit = {
    state.getById(id).fold(sender ! VerificationNotFound) { x =>
      if (x.code == code) {
        x match {
          case SmsVerification.Voted(_, _, _, apId, bulletin, _, _) => apsActor ! ApsActor.UpdateBulletinCmd(apId, bulletin)
          case _ =>
        }
        deleteVerification(state, id)
      } else sender() ! InvalidCode
    }
  }

  def behavior(state: SmsVerificationServiceState): Receive = {
    case AddVerificationCmd(x) => addVerification(state, x)
    case DeleteVerificationCmd(x) => deleteVerification(state, x)
    case GetVerification(x) => findVerification(state, x)
    case GetVerifications => findVerifications(state)
    case VerifyCmd(x, y) => verify(state, x, y)
  }

  override def afterRecover(state: SmsVerificationServiceState): Unit =
    state.getAll.values.foreach { x =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
    }
}

object SmsVerificationServiceActor {
  trait Command extends PersistentCommand
  trait Request extends PersistentQuery
  trait Response extends PersistentResponse
  trait Event extends PersistentEvent

  case class AddVerificationCmd(x: SmsVerification) extends Command
  case class DeleteVerificationCmd(id: Verification.Id) extends Command
  case class VerifyCmd(id: Verification.Id, code: String) extends Command

  case class GetVerification(id: Verification.Id) extends Request
  case object GetVerifications extends Request

  case class AddedVerificationEvt(x: SmsVerification) extends PersistentEvent
  case class DeletedVerificationEvt(id: Verification.Id) extends PersistentEvent

  case object Done extends Response
  case object VerificationAlreadyExists extends Response
  case object VerificationNotFound extends Response
  case class VerificationOpt(x: Option[SmsVerification])
  case class VerificationsRes(x: Map[Verification.Id, SmsVerification]) extends Response
  case object InvalidCode

  def props(
    id: String,
    apsActor: ActorRef,
    smsNotificationServiceActor: ActorRef,
    state: SmsVerificationServiceState = SmsVerificationServiceState.empty)(implicit c: ExecutionContext, t: Timeout): Props =
    Props(new SmsVerificationServiceActor(id, apsActor, smsNotificationServiceActor, state))
}

