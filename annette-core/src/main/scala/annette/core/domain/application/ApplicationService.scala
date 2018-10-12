package annette.core.domain.application

import akka.Done
import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import annette.core.domain.application.Application._
import javax.inject._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ApplicationService @Inject() (@Named("CoreService") actor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) {
  def create(application: Application): Future[Application] = {
    ask(actor, CreateApplicationCmd(application))
      .mapTo[Response]
      .map({
        case EntryAlreadyExists => throw new ApplicationAlreadyExists
        case ApplicationCreated(x) => x
      })
  }

  def update(application: UpdateApplication): Future[Unit] = {
    for {
      f <- ask(actor, UpdateApplicationCmd(application))
    } yield {
      f match {
        case Done =>
        case EntryNotFound => throw new ApplicationNotFound()
      }
    }
  }

  def getApplicationById(id: Application.Id): Future[Option[Application]] = {
    ask(actor, GetApplicationById(id)).mapTo[ApplicationOpt].map(_.x)
  }

  def selectAll: Future[List[Application]] = {
    ask(actor, ListApplications).mapTo[ApplicationsMap].map(_.x.values.toList)
  }

  def delete(id: Id): Future[Unit] = {
    for {
      f <- ask(actor, DeleteApplicationCmd(id))
    } yield {
      f match {
        case Done =>
        case EntryNotFound => throw new ApplicationNotFound()
      }
    }
  }
}

object ApplicationService {
  def props = Props(new ApplicationManagerActor())
}

