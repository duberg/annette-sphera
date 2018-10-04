
package annette.core.domain
import java.util.UUID

import akka.testkit.TestKit
import annette.core.domain.application.ApplicationManager
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewApplication { _: PersistenceSpec with TestKit =>

  def newApplicationActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(ApplicationManager.props(s"Application-$uuid"), s"application-$uuid")
  }
}
