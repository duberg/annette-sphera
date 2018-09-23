package annette.core.domain

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.SessionHistoryService
import annette.core.domain.tenancy.model._
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

import scala.util.Random

trait NewSessionHistory { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newSessionHistory = SessionHistory(
    userId = UUID.randomUUID(),
    tenantId = "tenant" + random.nextInt(),
    applicationId = "application" + random.nextInt(),
    languageId = "lang" + random.nextInt(),
    startTimestamp = DateTime.now(),
    endTimestamp = DateTime.now(),
    ip = "localhost",
    id = UUID.randomUUID())

  def newSessionHistoryActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(SessionHistoryService.props(s"SessionHistory-$uuid"), s"session-history-$uuid")
  }

}
