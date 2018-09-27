package annette.core.akkaext.persistence

import java.time.{ Instant, LocalDateTime, ZoneId }

import akka.actor.ActorLogging
import akka.persistence.{ PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotMetadata }

trait AfterSnapshotBehavior extends ActorLogging { _: PersistentActor =>
  private def d(timestamp: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString

  def afterSnapshot(metadata: SnapshotMetadata, success: Boolean): Unit = {}

  /**
   * === Поведение актора ===
   *
   * Поведение актора - это композиция из PartialFunction
   */

  def afterSnapshotBehavior: Receive = {
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
}
