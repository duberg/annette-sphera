package annette.core.domain

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.{LastSessionService, OpenSessionService, SessionHistoryService}
import annette.core.domain.tenancy.model.OpenSession
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

import scala.util.Random

trait NewOpenSession { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newOpenSession = OpenSession(
    userId = UUID.randomUUID(),
    tenantId = "tenant"+random.nextInt(),
    applicationId = "application"+random.nextInt(),
    languageId = "lang"+random.nextInt(),
    startTimestamp = DateTime.now(),
    lastOpTimestamp = DateTime.now(),
    rememberMe = random.nextBoolean(),
    timeout = 0,
    ip = "localhost"
  )

  def lastSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LastSessionService.props(s"LastSession-$uuid"), s"last-session-$uuid")
  }

  def sessionHistoryActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(SessionHistoryService.props(s"SessionHistory-$uuid"), s"session-history-$uuid")
  }

  def newOpenSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(OpenSessionService.props(s"OpenSession-$uuid", lastSessionActor(), sessionHistoryActor()),
      s"open-session-$uuid")
  }

}
