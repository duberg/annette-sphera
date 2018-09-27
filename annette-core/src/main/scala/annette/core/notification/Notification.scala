package annette.core.notification

import java.util.UUID

import annette.core.domain.tenancy.model.User

sealed trait Notification {
  def id: Notification.Id
  def message: String
  def retry: Int
}

object Notification {
  type Id = UUID
}

sealed trait EmailNotificationLike extends Notification {
  def email: String
  def subject: String
  def message: String
}

trait CreateNotification {
  def message: String
  def subject: String
}

trait CreateEmailNotificationLike extends CreateNotification {
  def email: String
}
trait CreateSmsNotificationLike extends CreateNotification {
  def phone: String
}
trait CreateWebSocketNotificationLike extends CreateNotification

/**
 * Обычное текстовое уведомление
 */
case class EmailNotification(
  id: Notification.Id,
  email: String,
  subject: String,
  message: String,
  retry: Int = 2) extends EmailNotificationLike

case class CreateEmailNotification(
  email: String,
  subject: String,
  message: String) extends CreateEmailNotificationLike

/**
 * Уведомление с паролем
 * Обрабатывается особым образом
 */
case class SendPasswordToEmailNotification(
  id: Notification.Id,
  email: String,
  subject: String,
  message: String,
  password: String,
  retry: Int = 2) extends EmailNotificationLike

case class CreateSendPasswordToEmailNotification(
  email: String,
  subject: String,
  message: String,
  password: String) extends CreateEmailNotificationLike

/**
 * Уведомление с подтверждением
 * Обрабатывается особым образом
 */
case class VerifyByEmailNotification(
  id: Notification.Id,
  email: String,
  subject: String,
  message: String,
  code: String,
  retry: Int = 2) extends EmailNotificationLike

case class CreateVerifyByEmailNotification(
  email: String,
  subject: String,
  message: String,
  code: String) extends CreateEmailNotificationLike

sealed trait SmsNotificationLike extends Notification {
  def phone: String
  def subject: String
  def message: String
  def retry: Int
}

/**
 * Уведомление с паролем
 * Обрабатывается особым образом
 */
case class SendPasswordToPhoneNotification(
  id: Notification.Id,
  phone: String,
  subject: String,
  message: String,
  password: String,
  retry: Int = 2) extends SmsNotificationLike

case class CreateSendPasswordToPhoneNotification(
  phone: String,
  subject: String,
  message: String,
  password: String) extends CreateSmsNotificationLike

/**
 * Обычное текстовое уведомление
 */
case class SmsNotification(
  id: Notification.Id,
  phone: String,
  subject: String,
  message: String,
  retry: Int = 2) extends SmsNotificationLike

case class CreateSmsNotification(
  phone: String,
  subject: String,
  message: String) extends CreateSmsNotificationLike

case class VerifyBySmsNotification(
  id: Notification.Id,
  phone: String,
  subject: String,
  message: String,
  code: String,
  retry: Int = 2) extends SmsNotificationLike

case class CreateVerifyBySmsNotification(
  phone: String,
  subject: String,
  message: String,
  code: String) extends CreateSmsNotificationLike

sealed trait WebSocketNotificationLike extends Notification {
  def userIds: Set[User.Id]
}

/**
 * Обычное текстовое уведомление
 */
case class WebSocketNotification(
  id: Notification.Id,
  userIds: Set[User.Id],
  message: String,
  retry: Int = 2) extends WebSocketNotificationLike
