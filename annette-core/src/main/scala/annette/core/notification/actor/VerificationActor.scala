package annette.core.notification.actor

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import annette.core.notification.actor.VerificationActor._
import annette.core.notification.{CreateVerification, NotificationManager, Verification, VerifyBySmsNotification}
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

private class VerificationActor(
                                 val id: ActorId,
                                 val initState: VerificationState)(implicit val executor: ExecutionContext, val timeout: Timeout) extends CqrsPersistentActor[VerificationState] {

  def createVerification(state: VerificationState, x: CreateVerification): Unit = {
    if (state.exists(x.id)) sender ! VerificationAlreadyExists else {
      persist(CreatedVerificationEvt(x)) { event =>
        changeState(state.updated(event))




        //smsNotificationActor ! SmsNotificationActor.CreateNotificationCmd(x.notification)
        context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
        sender ! Done
      }
    }
  }

  def deleteVerification(state: VerificationState, id: Verification.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedVerificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! VerificationNotFound
  }

  def findVerification(state: VerificationState, id: Verification.Id): Unit =
    sender() ! VerificationOpt(state.getById(id))

  def listVerifications(state: VerificationState): Unit =
    sender() ! VerificationMap(state.getAll)

  def verify(state: VerificationState, id: Verification.Id, code: String): Unit = {
    state.getById(id).fold(sender ! VerificationNotFound) { x =>
      if (x.code == code) deleteVerification(state, id)
      else sender() ! InvalidCode
    }
  }

  def behavior(state: VerificationState): Receive = {
    case CreateVerificationCmd(x) => createVerification(state, x)
    case DeleteVerificationCmd(x) => deleteVerification(state, x)
    case GetVerification(x) => findVerification(state, x)
    case ListVerifications => listVerifications(state)
    case VerifyCmd(x, y) => verify(state, x, y)
  }

  override def afterRecover(state: VerificationState): Unit =
    state.getAll.values.foreach { x =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
    }
}

object VerificationActor {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateVerificationCmd(x: CreateVerification) extends Command
  case class DeleteVerificationCmd(x: Verification.Id) extends Command
  case class VerifyCmd(x: Verification.Id, code: String) extends Command

  case class GetVerification(x: Verification.Id) extends Query
  case object ListVerifications extends Query

  case class CreatedVerificationEvt(x: Verification) extends Event
  case class DeletedVerificationEvt(x: Verification.Id) extends Event

  case object Done extends Response
  case class CreateVerificationSuccess(x: Verification) extends Response

  case object VerificationAlreadyExists extends Response
  case object VerificationNotFound extends Response
  case class VerificationOpt(x: Option[Verification]) extends Response
  case class VerificationMap(x: Map[Verification.Id, Verification]) extends Response
  case object InvalidCode extends Response

  def props(
    id: ActorId,
    state: VerificationState = VerificationState.empty)(implicit c: ExecutionContext, t: Timeout): Props =
    Props(new VerificationActor(id, state))
}

