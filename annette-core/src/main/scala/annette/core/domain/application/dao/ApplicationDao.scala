package annette.core.domain.application.dao

import javax.inject._

import akka.Done

import scala.util.{ Failure, Success }
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.application._
import annette.core.domain.application.model.Application.Id
import annette.core.domain.application.model.{ Application, ApplicationUpdate }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ApplicationDao @Inject() (
  @Named("CoreService") actor: ActorRef) extends IApplicationDao {

  override def create(application: Application)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, ApplicationService.CreateApplicationCmd(application))
    } yield {
      f match {
        case Done =>
        case ApplicationService.EntryAlreadyExists => throw new ApplicationAlreadyExists()
      }
    }
  }

  override def update(application: ApplicationUpdate)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, ApplicationService.UpdateApplicationCmd(application))
    } yield {
      f match {
        case Done =>
        case ApplicationService.EntryNotFound => throw new ApplicationNotFound()
      }
    }
  }

  override def getById(id: Application.Id)(implicit ec: ExecutionContext): Future[Option[Application]] = {
    ask(actor, ApplicationService.FindApplicationById(id)).mapTo[ApplicationService.SingleApplication].map(_.maybeEntry)
  }

  override def selectAll(implicit ec: ExecutionContext): Future[List[Application]] = {
    ask(actor, ApplicationService.FindAllApplications).mapTo[ApplicationService.MultipleApplications].map(_.entries.values.toList)
  }

  override def delete(id: Id)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      f <- ask(actor, ApplicationService.DeleteApplicationCmd(id))
    } yield {
      f match {
        case Done =>
        case ApplicationService.EntryNotFound => throw new ApplicationNotFound()
      }
    }
  }
}
