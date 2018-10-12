package annette.imc.persistence

import java.time.{ Instant, LocalDateTime, ZoneId }
import java.util.UUID

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.pattern.AskSupport
import akka.persistence._
import akka.util.Timeout
import annette.core.utils.ActorLifecycleHooks

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object Persistence extends AskSupport {
  /**
   * Predefined snapshot interval.
   */
  val SnapshotInterval = 1000

  /**
   * Паттерн Publisher-Subscriber.
   * Map with subscribers ActorRefs.
   * Subscribers actors could be inside or outside actor context.
   */
  type Subscribers = Map[UUID, ActorRef]

  /**
   * Команда меняет состояние актора
   */
  trait PersistentCommand

  /**
   * Запрос возвращает данные и не меняет состояние актора
   */
  trait PersistentQuery

  trait PersistentResponse

  trait PersistentEvent

  case object Ping extends PersistentQuery
  case object Kill extends PersistentQuery
  case class CreateChild(x: Props, y: String) extends PersistentQuery

  case object GetSubscriber extends PersistentQuery
  case class AddSubscriber(x: UUID, y: ActorRef) extends PersistentQuery
  case class RemoveSubscriberByActorRef(x: ActorRef) extends PersistentQuery
  case class RemoveSubscriberByUUID(x: UUID) extends PersistentQuery

  case object Pong extends PersistentResponse
  case object Terminated extends PersistentResponse
  case object Success extends PersistentResponse
  case object Persisted extends PersistentResponse
  case object NotPersisted extends PersistentResponse
  case class SubscribersMap(x: Subscribers) extends PersistentResponse

  case class InitializedEvt(state: AnyRef) extends PersistentEvent

  /**
   * Base trait for persistence actor state.
   *
   * @tparam T self type
   */
  trait PersistentState[T <: PersistentState[T]] { self: T =>
    def updated(event: PersistentEvent): T
  }

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
   *  case class ProcessRuntimeState(v: Map[ProcessInstance.Id, ProcessInfo] = Map.empty)
   *
   *  sealed trait Event extends PersistentEvent
   *  case class CreatedProcessInfoEvt(x: UpdateProcessInfo, t: ProcessTemplate.Id) extends Event
   *  case class UpdatedProcessInfoEvt(x: UpdateProcessInfo) extends Event
   *  case class DeletedProcessInfoEvt(id: ProcessInstance.Id) extends Event
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
   * To add related ActorRef use method: [[PersistentStateActor#addRelated]].
   * To remove related ActorRef use method: [[PersistentStateActor#removeRelated]].
   *
   * == Tests ==
   *
   * Use PersistenceSpec as base trait for idiomatic persistence tests.
   *
   */
  trait PersistentStateActor[T <: PersistentState[T]] extends PersistentActor with ActorLogging with ActorLifecycleHooks {
    private var recoveryOpt: Option[T] = None
    private var terminateOpt: Option[(ActorRef, Any)] = None
    private var subscribers: Subscribers = Map.empty

    def id: String
    def persistenceId: String = id

    def initState: T

    /**
     * Поведение актора которое необходимо реализовать.
     */
    def behavior(state: T): Receive

    def snapshotInterval: Int = SnapshotInterval

    def afterRecover(state: T): Unit = {}

    def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = {}

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

    def throwableBehavior: Receive = { case e: Exception => throw e }

    def creatorBehavior: Receive = {
      case p: Props =>
        val a = context.actorOf(p)
        sender() ! a
      case (props: Props, name: String) =>
        val a = context.actorOf(props, name)
        sender() ! a
    }

    /**
     * Поведение: Повторение событий при восстановлении и получении новых событий.
     *
     * Повторяет события при восстановлении актора, меняя состояние в функциональном стиле.
     */
    def persistBehavior(state: T): Receive = {
      case evt: PersistentEvent => persist(evt) { event =>
        changeState(state.updated(event))
        replyPersisted()
      }
    }

    def subscribersBehavior: Receive = {
      case GetSubscriber =>
        sender() ! SubscribersMap(subscribers)
      case AddSubscriber(x, y) =>
        subscribers += x -> y
        replySuccess()
      case RemoveSubscriberByUUID(x) =>
        subscribers -= x
        replySuccess()
      case RemoveSubscriberByActorRef(x) =>
        subscribers = subscribers.filterNot(_._2 == x)
        replySuccess()
    }

    def echoBehavior: Receive = {
      case "ping" => sender() ! "pong"
      case Ping => sender() ! Pong
    }

    /**
     * Поведение: Правильное завершение актора.
     *
     * Необходимо использовать в тестах.
     */
    def terminateBehavior: Receive = {
      case "kill" =>
        context.stop(self)
        terminateOpt = Option(sender() -> "terminated")
      case Kill =>
        context.stop(self)
        terminateOpt = Option(sender() -> Terminated)
    }

    /**
     * Поведение: Автоматическое создание снимка состояния.
     *
     * По заданному интервалу делаем snapshot.
     * Проверку делаем только для команды записи.
     */
    def saveSnapshotBehavior(state: T): PartialFunction[Any, Any] = {
      case cmd: PersistentCommand if lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0 =>
        saveSnapshot(state)
        cmd
      case cmd => cmd
    }

    def notMatchedBehavior: Receive = {
      case _ =>
    }

    /**
     * Активный контекст актора.
     */
    def active(state: T): Receive =
      saveSnapshotBehavior(state)
        .andThen { cmd =>
          behavior(state)
            .orElse(recoverFromSnapshotBehavior)
            .orElse(creatorBehavior)
            .orElse(persistBehavior(state))
            .orElse(subscribersBehavior)
            .orElse(throwableBehavior)
            .orElse(echoBehavior)
            .orElse(terminateBehavior)
            .orElse(notMatchedBehavior)(cmd)
        }

    /**
     * Обновление состояния актора
     */
    def changeState(state: T): Unit = context.become(active(state))

    def receiveCommand: Receive = active(initState)

    def receiveRecover: Receive = {
      case s: T @unchecked =>
        recoveryOpt = Option(s)
        changeState(s)
        log.info("Initialization completed")
      case event: PersistentEvent =>
        recoveryOpt = recoveryOpt.map(_.updated(event))
        changeState(recoveryOpt.get)
      case SnapshotOffer(SnapshotMetadata(pid, sequenceNr, timestamp), snapshot: T @unchecked) =>
        recoveryOpt = Option(snapshot)
        changeState(snapshot)
        log.info(s"Snapshot {{sequenceNr:$sequenceNr, ${d(timestamp)}} offered")
      case RecoveryCompleted =>
        val s = recoveryOpt.getOrElse(initState)
        if (recoveryOpt.nonEmpty) recoveryOpt = None
        else persist(initState) { _ => }
        log.info("Recovery completed")
        afterRecover(s)
    }

    def d(timestamp: Long): String =
      LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString

    def replyPersisted(): Unit = sender() ! Persisted

    def replySuccess(): Unit = if (sender() != self) sender() ! Success

    /**
     * === Publisher-Subscriber ===
     *
     * Единый механизм в функциональном стиле для работы со связанными акторами.
     * Используется вместо переменной var со списком ActorRefs.
     */
    def subscribersMap: Subscribers = subscribers

    def subscribersRefs: Seq[ActorRef] = subscribers.values.toSeq

    def addSubscriber(selfRef: ActorRef, x: UUID, y: ActorRef): Unit =
      selfRef ! AddSubscriber(x, y)

    def removeSubscriber(selfRef: ActorRef, x: ActorRef): Unit =
      selfRef ! RemoveSubscriberByActorRef(x)

    def removeSubscriber(selfRef: ActorRef, x: UUID): Unit =
      selfRef ! RemoveSubscriberByUUID(x)

    /**
     * Publish event to main event bus.
     * Very useful for tests.
     *
     * @param event persistent event
     */
    def publishEvt(event: PersistentEvent): Unit = context.system.eventStream.publish(event)

    // === Future API ===

    def createChild(selfRef: ActorRef, props: Props)(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, props).mapTo[ActorRef]

    def createChild(selfRef: ActorRef, props: Props, name: String)(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, (props, name)).mapTo[ActorRef]

    def createChild(selfRef: ActorRef, x: (Props, String))(implicit t: Timeout): Future[ActorRef] =
      ask(selfRef, (x._1, x._2)).mapTo[ActorRef]

    /**
     * Persist event in Future.
     * Don't forget to createUser local val with self ActorRef outside Future context.
     */
    def persistEvt(selfRef: ActorRef, event: PersistentEvent)(implicit e: ExecutionContext): Future[PersistentEvent] =
      Future {
        selfRef ! event
        event
      }

    /**
     * Send async command in future.
     * Useful when actor doesn't responding.
     */
    def sendCmd(toRef: ActorRef, cmd: PersistentCommand)(implicit e: ExecutionContext): Future[PersistentResponse] =
      Future {
        toRef ! cmd
        Success
      }

    override def postStop(): Unit = {
      terminateOpt.foreach {
        // Required to correctly shutdown persistence layer
        case (x, y) => context.system.scheduler.scheduleOnce(50 milliseconds, x, y)(context.dispatcher)
      }
      log.info("Terminated")
    }
  }
}
