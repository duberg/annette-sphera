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

trait CreateNotification
trait CreateEmailNotification
trait CreateSmsNotification
trait CreateWebSocketNotification

/**
 * Обычное текстовое уведомление
 */
case class EmailNotification(
  id: Notification.Id,
  email: String,
  subject: String,
  message: String,
  retry: Int = 10) extends EmailNotificationLike

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
  retry: Int = 10) extends EmailNotificationLike

case class CreateSendPasswordToEmailNotification(
                                            email: String,
                                            subject: String,
                                            message: String,
                                            password: String) extends CreateEmailNotification

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
  retry: Int = 10) extends EmailNotificationLike

case class CreateVerifyByEmailNotification(
                          email: String,
                          subject: String,
                          message: String,
                          code: String) extends CreateEmailNotification

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
  retry: Int = 10) extends SmsNotificationLike

/**
 * Обычное текстовое уведомление
 */
case class SmsNotification(
  id: Notification.Id,
  phone: String,
  subject: String,
  message: String,
  retry: Int = 10) extends SmsNotificationLike

case class VerifyBySmsNotification(
  id: Notification.Id,
  phone: String,
  subject: String,
  message: String,
  code: String,
  retry: Int = 10) extends SmsNotificationLike

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
  retry: Int = 10) extends WebSocketNotificationLike
