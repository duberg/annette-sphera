package annette.core.akkaext.persistence

import java.time.{ Instant, LocalDateTime, ZoneId }
import java.util.UUID

import akka.actor.{ ActorRef, Props }
import akka.persistence._
import akka.util.Timeout
import annette.core.akkaext.actor.CqrsQuery._
import annette.core.akkaext.actor.CqrsResponse._
import annette.core.akkaext.actor.{ CqrsActorBase, _ }
import annette.core.akkaext.persistence.CqrsPersistentActorLike._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util
import scala.util.{ Failure, Success, Try }
import scala.reflect.ClassTag

/**
 * = Actor in pure functional style =
 *
 * You must set constructor parameters `id`, `initState` and implement `behavior`.
 * When recovering replays events and snapshot, then updates state step by step.
 *
 * - No shared mutable state.
 * - Initial actor state `initState`, very useful.
 * - Автоматически делает snapshot по заданному интервалу.
 * - Создает дочерний актор в своем контексте, если ему прислать Props или (Props, Name).
 * - Обрабатывает ошибки, удобно при использовании supervisor strategy.
 * - Can reply on [[Ping]] request.
 * - Incoming events are persisted.
 * - Terminates on [[Kill]] message and reply [[Terminated]], very useful for async future tests.
 *
 * == Persistent events should be clearly mapped to state entities ==
 *
 * For example, if state stores ProcessInfo entities, there should be events:
 *
 * {{{
 *  case class ProcessRuntimeState(v: Map[ProcessInstance.ActorId, ProcessInfo] = Map.empty)
 *
 *  sealed trait CqrsEvent extends CqrsEvent
 *  case class CreatedProcessInfoEvt(x: UpdateProcessInfo, t: ProcessTemplate.ActorId) extends CqrsEvent
 *  case class UpdatedProcessInfoEvt(x: UpdateProcessInfo) extends CqrsEvent
 *  case class DeletedProcessInfoEvt(id: ProcessInstance.ActorId) extends CqrsEvent
 * }}}
 *
 * Follow this convention rule for Persistent event names.
 *
 * == Future API ==
 *
 * Use Future Api inside future.
 *
 * == Store ActorRef inside Actor ==
 *
 * Related ActorRefs are not Persisted.
 * To add related ActorRef use method: [[CqrsPersistentActorLike#addRelated]].
 * To remove related ActorRef use method: [[CqrsPersistentActorLike#removeRelated]].
 *
 * == Tests ==
 *
 * Use PersistenceSpec as base trait for idiomatic persistence tests.
 *
 */
trait CqrsPersistentActorLike extends PersistentActor with CqrsActorBase {
  private var recoveryOpt: Option[State] = None

  def persistenceId = id.raw

  def snapshotInterval: Int = SnapshotInterval
  def afterRecover(state: State): Unit = {}
  def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = {}

  /**
   * Persist method that auto changes state.
   */
  def persist[S <: State, E <: CqrsEvent](state: S, event: E)(code: (S, E) => Unit): Unit = {
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
  def persistOnly[S <: State, E <: CqrsEvent](state: S, event: E): Unit = {
    persist(state, event)((state, event) => Unit)
  }

  /**
   * Persist after Future result and auto changes state.
   */
  def persistAfter[R, E <: CqrsEvent](f: Future[R], event: E)(handler: (R, State, E) => Unit)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f flatMap { result => self.ask(PersistAfterCmd(result, event, handler)) } recover exceptionHandler pipeTo sender()
  }

  case class PersistAfterCmd[R, E <: CqrsEvent](result: R, event: E, handler: (R, State, E) => Unit) extends CqrsCommand

  implicit class PersistentActorRefOpt(actorRef: ActorRef)(implicit c: ExecutionContext, t: Timeout) {
    def persist(event: CqrsEvent): Future[CqrsEvent] = actorRef.ask(event).map(_ => event)
  }

  /**
   * === Поведение актора ===
   *
   * Поведение актора - это композиция из PartialFunction
   */

  def recoverFromSnapshotBehavior: Receive = {
    case m @ SaveSnapshotSuccess(SnapshotMetadata(pid, sequenceNr, timestamp)) =>
      log.info(s"New snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}}} saved")
      afterSnapshot(m.metadata, success = true)
    case m @ SaveSnapshotFailure(SnapshotMetadata(pid, sequenceNr, timestamp), reason) =>
      log.error(
        s"""Saving snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}}} failed
           |reason: $reason
           """.stripMargin)
      afterSnapshot(m.metadata, success = false)
  }

  /**
   * Поведение: Повторение событий при восстановлении и получении новых событий.
   *
   * Повторяет события при восстановлении актора, меняя состояние в функциональном стиле.
   */
  def persistBehavior(state: State): Receive = {
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

  override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistFailure(cause, event, seqNr)
    cause.printStackTrace()
    self ! cause
  }

  override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistRejected(cause: Throwable, event: Any, seqNr: Long)
    cause.printStackTrace()
    self ! cause
  }

  /**
   * Поведение: Автоматическое создание снимка состояния.
   *
   * По заданному интервалу делаем snapshot.
   * Проверку делаем только для команды записи.
   */
  def saveSnapshotBehavior[T](state: State): PartialFunction[T, T] = {
    case message @ (_: CqrsCommand | _: CqrsEvent) if lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0 =>
      saveSnapshot(state)
      message
    case message => message
  }

  def afterSaveSnapshotBehavior(state: State): Receive = {
    defaultBehavior(state)
      .orElse(afterBehavior(state))
      .orElse(getStateBehavior(state))
      .orElse(recoverFromSnapshotBehavior)
      .orElse(creatorBehavior)
      .orElse(persistBehavior(state))
      .orElse(publishBehavior)
      .orElse(throwableBehavior)
      .orElse(echoBehavior)
      .orElse(terminateBehavior)
      .orElse(notMatchedBehavior)
  }

  def ctx(state: State): Receive =
    saveSnapshotBehavior(state).andThen(afterSaveSnapshotBehavior(state))

  /**
   * Actor active context
   */
  def receiveContext(state: State) = ctx(state)

  def receiveCommand: Receive = receiveContext(initState)

  def receiveRecover: Receive = {
    case state: State @unchecked =>
      recoveryOpt = Option(state)
      changeState(state)
      log.info("Initialization completed")
    case event: CqrsEvent =>
      recoveryOpt = recoveryOpt.map(_.updated(event))
      changeState(recoveryOpt.get)
    case SnapshotOffer(SnapshotMetadata(pid, sequenceNr, timestamp), snapshot: State @unchecked) =>
      recoveryOpt = Option(snapshot)
      changeState(snapshot)
      log.info(s"Snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}} offered")
    case RecoveryCompleted =>
      val state = recoveryOpt.getOrElse(initState)
      if (recoveryOpt.nonEmpty) recoveryOpt = None
      else persist(initState) { _ => }
      log.info("Recovery completed")
      afterRecover(state)
  }

  private def d(timestamp: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString

  def replyPersisted(): Unit = sender() ! Persisted

  override def postStop(): Unit = {
    terminateOpt.foreach {
      // Required to correctly shutdown persistence layer
      case (x, y) => context.system.scheduler.scheduleOnce(50 milliseconds, x, y)(context.dispatcher)
    }
    log.info("Terminated")
  }
}

object CqrsPersistentActorLike {
  val SnapshotInterval = 1000
}

trait CqrsPersistentActor[S <: CqrsState] extends CqrsPersistentActorLike {
  type State = S
}