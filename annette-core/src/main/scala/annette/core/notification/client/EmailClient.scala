package annette.core.notification.client

import java.util.Properties

import annette.core.akkaext.actor.CqrsResponse
import annette.core.notification.EmailSettings
import annette.core.notification.client.EmailClient._
import javax.mail._
import javax.mail.internet.{ InternetAddress, MimeMessage }

import scala.util.{ Failure, Success, Try }

class EmailClient(val s: EmailSettings) {
  private var sessionOpt: Option[Session] = None
  private var transportOpt: Option[Transport] = None

  def settings: EmailSettings = s
  def session: Session = sessionOpt.get
  def connect(): Try[Response] = {
    if (!settings.debug) {
      transportOpt match {
        case Some(t) => Failure(EmailClient.TransportException("Transport already connected"))
        case None => Try {
          val props = new Properties()
          settings.smtp.foreach({ case (k, v) => props.put(k, v) })
          val s: Session = Session.getInstance(
            props,
            new Authenticator {
              override def getPasswordAuthentication = new PasswordAuthentication(
                settings.username,
                settings.password)
            })
          //s.setDebug(true)
          val t: Transport = s.getTransport
          t.connect()
          sessionOpt = Option(s)
          transportOpt = Option(t)
          Connected
        }
      }
    } else Success(Connected)
  }
  def createMessage(to: String, subject: String, message: String): MimeMessage = {
    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(settings.from))
    msg.setRecipients(
      Message.RecipientType.TO,
      InternetAddress.parse(to).asInstanceOf[Array[Address]])
    msg.setSubject(subject)
    msg.setText(message, "utf-8", "html")
    msg
  }
  def send(to: String, subject: String, message: String): Try[Response] = {
    if (!settings.debug) {
      transportOpt match {
        case Some(t) => Try {
          val m = createMessage(to, subject, message)
          t.sendMessage(m, m.getAllRecipients)
          SendSuccess
        }
        case None => Failure(EmailClient.TransportException("Transport doesn'template connected"))
      }
    } else Success(SendSuccess)
  }
  def disconnect(): Try[Response] = {
    if (!settings.debug) {
      Try {
        sessionOpt = None
        transportOpt = None
        transportOpt.foreach(_.close())
        Disconnected
      }
    } else Success(Disconnected)
  }
}

object EmailClient {
  trait Response extends CqrsResponse
  case object SendSuccess extends Response
  case object Connected extends Response
  case object Disconnected extends Response

  abstract class Exception(message: String) extends RuntimeException(message)
  case class TransportException(message: String) extends Exception(message)
}