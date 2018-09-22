/***************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  ****************************************************************************************/

package annette.core.domain.language.dao

import javax.inject._

import akka.Done

import scala.util.{ Failure, Success }
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.language._
import annette.core.domain.language.model.Language.Id
import annette.core.domain.language.model.{ Language, LanguageUpdate }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LanguageDao @Inject() (
  @Named("CoreService") actor: ActorRef) extends ILanguageDao {

  override def create(language: Language)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.CreateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryAlreadyExists => throw new LanguageAlreadyExists()
      }
    }
  }

  override def update(language: LanguageUpdate)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.UpdateLanguageCmd(language))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryNotFound => throw new LanguageNotFound()
      }
    }
  }

  override def getById(id: Language.Id)(implicit ec: ExecutionContext): Future[Option[Language]] = {
    ask(actor, LanguageService.FindLanguageById(id)).mapTo[LanguageService.SingleLanguage].map(_.maybeEntry)
  }

  override def selectAll(implicit ec: ExecutionContext): Future[List[Language]] = {
    ask(actor, LanguageService.FindAllLanguages).mapTo[LanguageService.MultipleLanguages].map(_.entries.values.toList)
  }

  override def delete(id: Id)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, LanguageService.DeleteLanguageCmd(id))
    } yield {
      f match {
        case Done =>
        case LanguageService.EntryNotFound => throw new LanguageNotFound()
      }
    }
  }
}
