package annette.core.domain

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.domain.tenancy.model.OpenSession
import annette.core.domain.tenancy.{ LastSessionManager, OpenSessionManager, SessionHistoryManager }
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewOpenSession { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newOpenSession = OpenSession(
    userId = UUID.randomUUID(),
    tenantId = "tenant" + random.nextInt(),
    applicationId = "application" + random.nextInt(),
    languageId = "lang" + random.nextInt(),
    startTimestamp = LocalDateTime.now(),
    lastOpTimestamp = LocalDateTime.now(),
    rememberMe = random.nextBoolean(),
    timeout = 0,
    ip = "localhost")

  def lastSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LastSessionManager.props, s"last-session-$uuid")
  }

  def sessionHistoryActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(SessionHistoryManager.props, s"session-history-$uuid")
  }

  def newOpenSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(
      props = OpenSessionManager.props(lastSessionActor(), sessionHistoryActor()),
      name = s"open-session-$uuid")
  }

}
