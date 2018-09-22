package annette.imc.notification.model

import java.util.UUID

trait Notification {
  def id: Notification.Id
  def language: String
  def retry: Int
}

object Notification {
  type Id = UUID
}

trait MailNotification extends Notification {
  def email: String
  def templateParameters: Map[String, String]
}

object MailNotification {
  case class Password(
    id: Notification.Id,
    email: String,
    password: String,
    language: String,
    templateParameters: Map[String, String],
    retry: Int = 3) extends MailNotification

  case class ToExpertise(
    id: Notification.Id,
    email: String,
    language: String,
    templateParameters: Map[String, String],
    retry: Int = 3) extends MailNotification

  case class ToReview(
    id: Notification.Id,
    email: String,
    language: String,
    templateParameters: Map[String, String],
    retry: Int = 3) extends MailNotification

  case class NotReady(
    id: Notification.Id,
    email: String,
    language: String,
    templateParameters: Map[String, String],
    retry: Int = 3) extends MailNotification
}

trait SmsNotification extends Notification {
  def phone: String
}

object SmsNotification {
  case class Password(
    id: Notification.Id,
    phone: String,
    password: String,
    language: String,
    retry: Int = 3) extends SmsNotification

  case class Verification(
    id: Notification.Id,
    phone: String,
    code: String,
    language: String,
    retry: Int = 5) extends SmsNotification

  case class ToExpertise(
    id: Notification.Id,
    phone: String,
    language: String,
    retry: Int = 3) extends SmsNotification

  case class ToReview(
    id: Notification.Id,
    phone: String,
    language: String,
    retry: Int = 3) extends SmsNotification
}

