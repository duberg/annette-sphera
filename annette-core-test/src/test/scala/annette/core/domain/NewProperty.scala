
package annette.core.domain

import java.util.UUID

import akka.testkit.TestKit
import annette.core.domain.property.PropertyService
import annette.core.test.PersistenceSpec

import scala.util.Random

trait NewProperty { _: PersistenceSpec with TestKit =>

  private val random = new Random()

  def newPropertyActor() = {
    val uuid = UUID.randomUUID().toString
    system.actorOf(PropertyService.props(s"Property-$uuid"), s"property-$uuid")
  }

}
