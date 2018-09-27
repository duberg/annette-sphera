package annette.core.notification

import annette.core.ModuleConfig
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration._

trait NotificationConfig {
  case class EmailNotificationEntry(retryInterval: FiniteDuration, email: EmailSettings)
  case class SmsNotificationEntry(retryInterval: FiniteDuration, sms: SmsSettings)

  implicit class RichConfig(val underlying: Config) extends ModuleConfig {
    def toEmailNotificationEntry(config: Config): EmailNotificationEntry = {
      // val retryInterval = config.getFiniteDuration("retry-interval")
      val emailConfig = config.getConfig("mail")
      val from = emailConfig.getString("from")
      val username = emailConfig.getString("username")
      val password = emailConfig.getString("password")
      val debug = config.getBooleanOpt("debug").getOrElse(false)
      val smtp: Map[String, AnyRef] = emailConfig.getConfig("smtp").entrySet().asScala
        .map(entry => (s"mail.smtp.${entry.getKey}", entry.getValue.unwrapped()))
        .toMap
      val mail = EmailSettings(
        smtp = smtp,
        from = from,
        username = username,
        password = password,
        debug = debug)
      EmailNotificationEntry(retryInterval = 6 second, email = mail)
    }

    def toSmsNotificationEntry(config: Config): SmsNotificationEntry = {
      // val retryInterval = config.getFiniteDuration("retry-interval")
      val smsConfig = config.getConfig("sms")
      val apiUrl = smsConfig.getString("api-url")
      val apiKey = smsConfig.getString("api-key")
      val apiSalt = smsConfig.getString("api-salt")
      val route = smsConfig.getString("route")
      val fromExtension = smsConfig.getString("from-extension")
      val debug = config.getBooleanOpt("debug").getOrElse(false)
      val sms = SmsSettings(
        apiUrl = apiUrl,
        apiKey = apiKey,
        apiSalt = apiSalt,
        route = route,
        fromExtension = fromExtension,
        debug = debug)
      SmsNotificationEntry(retryInterval = 1 second, sms = sms)
    }

    def emailNotificationEntry: EmailNotificationEntry =
      toEmailNotificationEntry(underlying.getConfig("notification"))

    def smsNotificationEntry: SmsNotificationEntry =
      toSmsNotificationEntry(underlying.getConfig("notification"))
  }
}

