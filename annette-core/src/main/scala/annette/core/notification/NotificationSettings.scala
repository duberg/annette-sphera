package annette.core.notification

trait NotificationSettings

case class EmailSettings(
  smtp: Map[String, AnyRef],
  from: String,
  username: String,
  password: String,
  debug: Boolean) extends NotificationSettings

case class SmsSettings(
  apiUrl: String,
  apiKey: String,
  apiSalt: String,
  route: String,
  fromExtension: String,
  debug: Boolean) extends NotificationSettings
