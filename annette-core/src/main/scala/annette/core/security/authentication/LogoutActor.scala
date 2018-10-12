package annette.core.security.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import akka.util.Timeout
import annette.core.domain.tenancy.SessionService

import scala.concurrent.ExecutionContext

class LogoutActor(sessionDao: SessionService)(implicit c: ExecutionContext, t: Timeout) extends Actor with ActorLogging {
  def receive: Receive = LoggingReceive {
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
