package annette.core.security.verification

import akka.actor.Props
import akka.util.Timeout
import annette.core.akkaext.actor._
import annette.core.akkaext.persistence._
import annette.core.security.verification.Verification._

import scala.concurrent.ExecutionContext

private class VerificationActor(
  val id: ActorId,
  val initState: VerificationState)(implicit val executor: ExecutionContext, val timeout: Timeout) extends CqrsPersistentActor[VerificationState] {

  def createVerification(state: VerificationState, x: CreateVerification): Unit = {
    val verification = Verification(
      id = generateUUID,
      code = x.code,
      duration = x.duration)
    persist(state, CreatedVerificationEvt(verification)) { (state, event) =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(verification.id))
      sender ! CreateVerificationSuccess(verification)
    }
  }

  def deleteVerification(state: VerificationState, id: Verification.Id): Unit = {
    if (state.verificationExists(id)) {
      persist(DeletedVerificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! VerificationNotFound
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
  def props(
    id: ActorId,
    state: VerificationState = VerificationState.empty)(implicit c: ExecutionContext, t: Timeout): Props =
    Props(new VerificationActor(id, state))
}

