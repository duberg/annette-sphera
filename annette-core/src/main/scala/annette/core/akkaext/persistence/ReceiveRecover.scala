package annette.core.akkaext.persistence

import java.time.{ Instant, LocalDateTime, ZoneId }

import akka.persistence.{ RecoveryCompleted, SnapshotMetadata, SnapshotOffer }
import annette.core.akkaext.actor.{ CqrsEvent, CqrsState }

import scala.concurrent.duration._

trait ReceiveRecover[A <: CqrsState] { _: CqrsPersistentActor[A] =>
  private var recoveryOpt: Option[A] = None
  private def d(timestamp: Long): String = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toString

  def afterRecover(state: A): Unit = {}

  def receiveRecover: Receive = {
    case state: A @unchecked =>
      recoveryOpt = Option(state)
      changeState(state)
      log.info("Initialization completed")
    case event: CqrsEvent =>
      recoveryOpt = recoveryOpt.map(_.updated(event))
      changeState(recoveryOpt.get)
    case SnapshotOffer(SnapshotMetadata(pid, sequenceNr, timestamp), snapshot: A @unchecked) =>
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

  override def postStop(): Unit = {
    terminateOpt.foreach {
      // Required to correctly shutdown persistence layer
      case (x, y) => context.system.scheduler.scheduleOnce(50 milliseconds, x, y)(context.dispatcher)
    }
    log.info("Terminated")
  }
}
