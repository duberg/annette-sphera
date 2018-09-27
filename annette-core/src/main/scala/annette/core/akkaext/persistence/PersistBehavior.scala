package annette.core.akkaext.persistence

import akka.actor.ActorRef
import akka.persistence.PersistentActor
import akka.util.Timeout
import annette.core.akkaext.actor.CqrsResponse.Persisted
import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsState }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait PersistBehavior[A <: CqrsState] { x: CqrsPersistentActor[A] with PersistentActor =>
  /**
   * Persist method that auto changes state.
   */
  def persist[S <: A, E <: CqrsEvent](state: S, event: E)(code: (S, E) => Unit): Unit = {
    persist(event) { event =>
      Try(state.updated(event)) match {
        case Success(x) =>
          changeState(x)
          code(x, event)
        case Failure(e) =>
          exceptionHandler(e)
      }
    }
  }

  /**
   * Persist method without `code`, auto changes state.
   */
  def persistOnly[S <: A, E <: CqrsEvent](state: S, event: E): Unit = {
    persist(state, event)((state, event) => Unit)
  }

  /**
   * Persist after Future result and auto changes state.
   */
  def persistAfter[R, E <: CqrsEvent](f: Future[R], event: E)(handler: (R, A, E) => Unit)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f flatMap { result => self.ask(PersistAfterCmd(result, event, handler)) } recover exceptionHandler pipeTo sender()
  }

  case class PersistAfterCmd[R, E <: CqrsEvent](result: R, event: E, handler: (R, A, E) => Unit) extends CqrsCommand

  implicit class PersistentActorRefOpt(actorRef: ActorRef)(implicit c: ExecutionContext, t: Timeout) {
    def persist(event: CqrsEvent): Future[CqrsEvent] = actorRef.ask(event).map(_ => event)
  }

  /**
   * Поведение: Повторение событий при восстановлении и получении новых событий.
   *
   * Повторяет события при восстановлении актора, меняя состояние в функциональном стиле.
   */
  def persistBehavior(state: A): Receive = {
    case evt: CqrsEvent => persist(evt) { event =>
      changeState(state.updated(event))
      replyPersisted()
    }
    case x: PersistAfterCmd[_, _] =>
      persist(x.event) { event =>
        val updated = state.updated(event)
        changeState(updated)
        x.handler(x.result, updated, event)
      }
  }

  def replyPersisted(): Unit = sender() ! Persisted
}
