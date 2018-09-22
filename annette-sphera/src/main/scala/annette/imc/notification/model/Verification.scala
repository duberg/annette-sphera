package annette.imc.notification.model

import java.util.UUID

import annette.imc.model.{ ApStatus, _ }

import scala.concurrent.duration.{ FiniteDuration, _ }

trait Verification {
  def id: Verification.Id
  def code: String
  def language: String
  def duration: FiniteDuration
}

object Verification {
  type Id = UUID
}

trait SmsVerification extends Verification {
  def phone: String
}

object SmsVerification {
  case class Status(
    id: Verification.Id,
    code: String,
    phone: String,
    language: String,
    duration: FiniteDuration = 10.minute) extends SmsVerification

  case class Voted(
    id: Verification.Id,
    code: String,
    phone: String,
    apId: Ap.Id,
    bulletin: UpdateBulletin,
    language: String,
    duration: FiniteDuration = 10.minute) extends SmsVerification
}