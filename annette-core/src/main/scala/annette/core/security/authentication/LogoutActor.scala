package annette.core.security.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import annette.core.domain.tenancy.SessionManager

/**
 * Created by valery on 25.10.16.
 */
class LogoutActor(
  sessionDao: SessionManager)
  extends Actor with ActorLogging {

  implicit val ec = context.dispatcher

  override def receive: Receive = LoggingReceive {
    case AuthenticationService.Logout(token) =>
      val requestor = sender()
      val future = sessionDao.closeSession(token)
      future.foreach { case _ => requestor ! AuthenticationService.LoggedOut(token) }
      future.failed.foreach {
        case throwable: Throwable =>
          requestor ! AuthenticationService.FailureResponse(throwable)
      }
  }

}
