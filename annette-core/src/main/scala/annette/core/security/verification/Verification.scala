package annette.core.security.verification

import java.util.UUID

import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }

import scala.concurrent.duration.{ FiniteDuration, _ }

case class Verification(
  id: Verification.Id,
  code: String,
  duration: FiniteDuration)

case class CreateVerification(
  code: String,
  duration: FiniteDuration)

object Verification {
  type Id = UUID

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
}