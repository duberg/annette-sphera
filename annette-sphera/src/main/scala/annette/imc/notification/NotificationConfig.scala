package annette.imc.notification

import annette.core.ModuleConfig
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration._

trait NotificationConfig {
  case class MailNotificationEntry(retryInterval: FiniteDuration, mail: MailSettings)
  case class SmsNotificationEntry(retryInterval: FiniteDuration, sms: SmsSettings)

  implicit class RichConfig(val underlying: Config) extends ModuleConfig {
    def toMailNotificationEntry(config: Config): MailNotificationEntry = {
      //val retryInterval = config.getFiniteDuration("retry-interval")
      val mailConfig = config.getConfig("mail")
      val from = mailConfig.getString("from")
      val username = mailConfig.getString("username")
      val password = mailConfig.getString("password")
      val debug = mailConfig.getBooleanOpt("debug").getOrElse(false)
      val smtp: Map[String, AnyRef] = mailConfig.getConfig("smtp").entrySet().asScala
        .map(entry => (s"mail.smtp.${entry.getKey}", entry.getValue.unwrapped()))
        .toMap
      val mail = MailSettings(
        smtp = smtp,
        from = from,
        username = username,
        password = password,
        debug = debug)
      MailNotificationEntry(6 second, mail)
    }

    def toSmsNotificationEntry(config: Config): SmsNotificationEntry = {
      //val retryInterval = config.getFiniteDuration("retry-interval")
      val smsConfig = config.getConfig("sms")
      val apiUrl = smsConfig.getString("api-url")
      val apiKey = smsConfig.getString("api-key")
      val apiSalt = smsConfig.getString("api-salt")
      val route = smsConfig.getString("route")
      val fromExtension = smsConfig.getString("from-extension")
      val debug = smsConfig.getBooleanOpt("debug").getOrElse(false)
      val sms = SmsSettings(
        apiUrl = apiUrl,
        apiKey = apiKey,
        apiSalt = apiSalt,
        route = route,
        fromExtension = fromExtension,
        debug = debug)
      SmsNotificationEntry(6 second, sms)
    }

    def mailNotificationEntry: MailNotificationEntry =
      toMailNotificationEntry(underlying.getConfig("notification"))

    def smsNotificationEntry: SmsNotificationEntry =
      toSmsNotificationEntry(underlying.getConfig("notification"))
  }
}

