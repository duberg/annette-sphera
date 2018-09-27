package annette.core.akkaext.persistence

import akka.persistence.PersistentActor
import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsMessage, CqrsState }

trait ActiveContext[A <: CqrsState] extends AfterSnapshotBehavior
  with PersistBehavior[A] { _: CqrsPersistentActor[A] =>

  /**
   * Поведение: Автоматическое создание снимка состояния.
   *
   * По заданному интервалу делаем snapshot.
   * Проверку делаем только для команды записи.
   */
  def saveSnapshotBehavior[T](state: A): PartialFunction[T, T] = {
    case message @ (_: CqrsCommand | _: CqrsEvent) if lastSequenceNr % snapshotInterval == 0 && lastSequenceNr != 0 =>
      saveSnapshot(state)
      message
    case message => message
  }

  def afterSaveSnapshotBehavior(state: A): Receive = {
    defaultBehavior(state)
      .orElse(afterBehavior(state))
      .orElse(getStateBehavior(state))
      .orElse(afterSnapshotBehavior)
      .orElse(creatorBehavior)
      .orElse(persistBehavior(state))
      .orElse(publishBehavior)
      .orElse(throwableBehavior)
      .orElse(echoBehavior)
      .orElse(terminateBehavior)
      .orElse(notMatchedBehavior)
  }

  /**
   * Actor active context
   */
  override def activeContext(state: A): Receive =
    saveSnapshotBehavior(state)
      .andThen(afterSaveSnapshotBehavior(state))

  def receiveCommand: Receive = activeContext(initState)
}
