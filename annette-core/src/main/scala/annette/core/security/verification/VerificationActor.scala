package annette.core.security.verification

import akka.actor.Props
import akka.util.Timeout
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._
import annette.core.security.verification.Verification._

import scala.concurrent.ExecutionContext

class VerificationActor(val bus: VerificationBus, val initState: VerificationState = VerificationState.empty)(implicit val c: ExecutionContext, val t: Timeout) extends CqrsPersistentActor[VerificationState] {
  def createVerification(state: VerificationState, x: CreateVerificationLike): Unit = {
    val verification = x match {
      case y: CreateVerification => Verification(
        id = generateUUID,
        code = y.code,
        duration = y.duration)
      case y: CreateEmailVerification => EmailVerification(
        id = generateUUID,
        code = y.code,
        email = y.email,
        duration = y.duration)
    }

    persist(state, VerificationCreatedEvt(verification)) { (state, event) =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(verification.id))
      sender ! CreateVerificationSuccess(verification)
    }
  }

  def deleteVerification(state: VerificationState, x: Verification.Id): Unit = {
    state.getVerificationById(x).fold(sender ! VerificationNotFound) { y =>
      persist(state, VerificationDeletedEvt(y.id)) { (state, event) =>
        y match {
          case z: Verification => bus.publish(VerifiedEvt(z))
          case z: EmailVerification => bus.publish(EmailVerifiedEvt(z))
        }
        sender ! Done
      }
    }
  }

  def findVerification(state: VerificationState, id: Verification.Id): Unit =
    sender() ! VerificationOpt(state.getVerificationById(id))

  def listVerifications(state: VerificationState): Unit =
    sender() ! VerificationMap(state.verificationsMap)

  def verify(state: VerificationState, id: Verification.Id, code: String): Unit = {
    state.getVerificationById(id).fold(sender ! VerificationNotFound) { x =>
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
    state.verificationsMap.values.foreach { x =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
    }
}

object VerificationActor {
  def props(bus: VerificationBus)(implicit c: ExecutionContext, t: Timeout): Props =
    Props(new VerificationActor(bus))
}

