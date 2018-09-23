/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */
package annette.core.domain

import javax.inject.{ Named, Singleton }

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import annette.core.domain.application.ApplicationService
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.{ LastSessionService, OpenSessionService, SessionHistoryService, UserService }

@Singleton
@Named("CoreService")
class CoreServiceActor extends Actor with ActorLogging {

  val applicationActor: ActorRef = context.actorOf(ApplicationService.props("core-application"), "application")
  val languageActor: ActorRef = context.actorOf(LanguageService.props("core-language"), "language")
  val userActor: ActorRef = context.actorOf(UserService.props("core-user"), "user")

  val lastSessionActor: ActorRef = context.actorOf(LastSessionService.props("core-last-session"), "last-session")
  val sessionHistoryActor: ActorRef = context.actorOf(SessionHistoryService.props("core-session-history"), "session-history")
  val openSessionActor: ActorRef = context.actorOf(OpenSessionService.props("core-open-session", lastSessionActor, sessionHistoryActor), "open-session")

  override def receive: PartialFunction[Any, Unit] = {
    case msg: ApplicationService.Command =>
      applicationActor forward msg
    case msg: ApplicationService.Query =>
      applicationActor forward msg
    case msg: LanguageService.Command =>
      languageActor forward msg
    case msg: LanguageService.Query =>
      languageActor forward msg
    case msg: UserService.Command =>
      userActor forward msg
    case msg: UserService.Query =>
      userActor forward msg
    case msg: OpenSessionService.Command =>
      openSessionActor forward msg
    case msg: OpenSessionService.Query =>
      openSessionActor forward msg
    case msg: LastSessionService.Command =>
      lastSessionActor forward msg
    case msg: LastSessionService.Query =>
      lastSessionActor forward msg
    case msg: SessionHistoryService.Command =>
      sessionHistoryActor forward msg
    case msg: SessionHistoryService.Query =>
      sessionHistoryActor forward msg
  }
}

object CoreService {
  val name = "core"

  def props = Props(classOf[CoreServiceActor])
}
