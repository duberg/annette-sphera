package annette.core.domain

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestKit
import annette.core.domain.language.LanguageService
import annette.core.domain.tenancy.LastSessionService
import annette.core.domain.tenancy.model.{ LastSession, OpenSession }
import annette.core.test.PersistenceSpec
import org.joda.time.DateTime

import scala.util.Random

trait NewLastSession { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newLastSession = LastSession(
    userId = UUID.randomUUID(),
    tenantId = "tenant" + random.nextInt(),
    applicationId = "application" + random.nextInt(),
    languageId = "lang" + random.nextInt(),
    startTimestamp = DateTime.now(),
    endTimestamp = DateTime.now(),
    ip = "localhost",
    id = UUID.randomUUID())

  def newLastSessionActor(): ActorRef = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LastSessionService.props(s"LastSession-$uuid"), s"last-session-$uuid")
  }

}
