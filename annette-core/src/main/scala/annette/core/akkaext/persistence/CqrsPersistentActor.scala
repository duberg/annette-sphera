package annette.core.akkaext.persistence

import akka.persistence._
import annette.core.akkaext.actor.CqrsQuery._
import annette.core.akkaext.actor.CqrsResponse._
import annette.core.akkaext.actor.{ CqrsActorBase, _ }

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
 * To add related ActorRef use method: [[CqrsPersistentActor#addRelated]].
 * To remove related ActorRef use method: [[CqrsPersistentActor#removeRelated]].
 *
 * == Tests ==
 *
 * Use PersistenceSpec as base trait for idiomatic persistence tests.
 *
 */
abstract class CqrsPersistentActor[A <: CqrsState] extends PersistentActor
  with CqrsActorBase[A]
  with ActiveContext[A]
  with ReceiveRecover[A] {
  /**
   * Init actor state
   */
  def initState: A

  /**
   * Поведение актора которое необходимо реализовать.
   */
  def behavior(state: A): Receive
}

object CqrsPersistentActor {
  val SnapshotInterval = 1000
}