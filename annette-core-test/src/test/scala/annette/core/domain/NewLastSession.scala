package annette.core.domain

import java.time.LocalDateTime
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.domain.tenancy.LastSessionManager
import annette.core.domain.tenancy.model.LastSession
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewLastSession { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newLastSession = LastSession(
    userId = UUID.randomUUID(),
    tenantId = "tenant" + random.nextInt(),
    applicationId = "application" + random.nextInt(),
    languageId = "lang" + random.nextInt(),
    startTimestamp = LocalDateTime.now(),
    endTimestamp = LocalDateTime.now(),
    ip = "localhost",
    id = UUID.randomUUID())

  def newLastSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LastSessionManager.props, s"last-session-$uuid")
  }

}
