package annette.core.security.verification

import java.util.UUID

import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }

import scala.concurrent.duration.FiniteDuration

trait VerificationLike {
  def id: Verification.Id
  def code: String
  def duration: FiniteDuration
}

trait CreateVerificationLike {
  def code: String
  def duration: FiniteDuration
}

case class Verification(
  id: Verification.Id,
  code: String,
  duration: FiniteDuration) extends VerificationLike

case class CreateVerification(
  code: String,
  duration: FiniteDuration) extends CreateVerificationLike

case class EmailVerification(
  id: Verification.Id,
  code: String,
  email: String,
  duration: FiniteDuration) extends VerificationLike

case class CreateEmailVerification(
  code: String,
  email: String,
  duration: FiniteDuration) extends CreateVerificationLike

object Verification {
  type Id = UUID

  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateVerificationCmd(x: CreateVerificationLike) extends Command
  case class DeleteVerificationCmd(x: Verification.Id) extends Command
  case class VerifyCmd(x: Verification.Id, code: String) extends Command

  case class GetVerification(x: Verification.Id) extends Query
  case object ListVerifications extends Query

  case class VerificationCreatedEvt(x: VerificationLike) extends Event
  case class VerificationDeletedEvt(x: Verification.Id) extends Event
  case class VerifiedEvt(x: Verification) extends Event
  case class EmailVerifiedEvt(x: EmailVerification) extends Event

  case object Done extends Response
  case class CreateVerificationSuccess(x: VerificationLike) extends Response
  case object VerificationAlreadyExists extends Response
  case object VerificationNotFound extends Response
  case class VerificationOpt(x: Option[VerificationLike]) extends Response
  case class VerificationMap(x: Map[Verification.Id, VerificationLike]) extends Response
  case object InvalidCode extends Response
}