
package annette.core.domain

import java.util.UUID

import akka.testkit.TestKit
import annette.core.domain.language.LanguageService
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewLanguage { _: PersistenceSpec with TestKit =>
  private val random = new Random()

  def newLanguageActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(LanguageService.props(s"Language-$uuid"), s"language--$uuid")
  }
}
