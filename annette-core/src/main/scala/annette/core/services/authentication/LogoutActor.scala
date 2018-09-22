/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.services.authentication

import akka.actor.{ Actor, ActorLogging }
import akka.event.LoggingReceive
import annette.core.domain.tenancy.dao.SessionDao

/**
 * Created by valery on 25.10.16.
 */
class LogoutActor(
  sessionDao: SessionDao)
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
