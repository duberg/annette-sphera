
package annette.core.domain

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestKit
import annette.core.domain.property.PropertyService
import annette.core.domain.property.model.Property
import annette.core.test.PersistenceSpec
import annette.core.utils.{ AnyValue, NoValue, Value }

class PropertyActorSpec extends TestKit(ActorSystem("PropertyActorSpec"))
  with PersistenceSpec with NewProperty {

  "A PropertyActor" when receive {
    "SetPropertyCmd" must {
      "set new property" in {
        val c1 = Property(tenantId = Some("tenant1"), key = "a", value = "value1")
        val c2 = Property(tenantId = Some("tenant2"), key = "a", value = "value1")
        val actor = newPropertyActor()
        for {
          cc1 <- ask(actor, PropertyService.SetPropertyCmd(c1))
          cc2 <- ask(actor, PropertyService.SetPropertyCmd(c2))
          ccs <- ask(actor, PropertyService.FindAllProperties).mapTo[PropertyService.PropertySeq].map(_.entries)
          ccr <- ask(actor, PropertyService.FindPropertyById(c2.id)).mapTo[PropertyService.PropertyOption].map(_.maybeEntry)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs should contain(c1)
          ccs should contain(c2)
          ccr shouldBe Some(c2)
        }
      }
    }

    "RemovePropertyCmd" must {
      "remove property" in {
        val c1 = Property(tenantId = Some("tenant1"), key = "a", value = "value1")
        val c2 = Property(tenantId = Some("tenant2"), key = "a", value = "value1")
        val actor = newPropertyActor()
        for {
          cc1 <- ask(actor, PropertyService.SetPropertyCmd(c1))
          cc2 <- ask(actor, PropertyService.SetPropertyCmd(c2))
          ccs <- ask(actor, PropertyService.FindAllProperties).mapTo[PropertyService.PropertySeq].map(_.entries)
          d1 <- ask(actor, PropertyService.RemovePropertyCmd(c1.id))
          ccr <- ask(actor, PropertyService.FindAllProperties).mapTo[PropertyService.PropertySeq].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          ccs should contain(c1)
          ccs should contain(c2)
          ccs should have length (2)
          d1 shouldBe Done
          ccr should contain(c2)
          ccr should have length (1)
        }
      }
      "should not deleteUser property if it does not exist" in {
        val c1 = Property(tenantId = Some("tenant1"), key = "a", value = "value1")
        val c2 = Property(tenantId = Some("tenant2"), key = "a", value = "value1")
        val actor = newPropertyActor()
        for {
          d1 <- ask(actor, PropertyService.RemovePropertyCmd(c1.id))
        } yield {
          d1 shouldBe PropertyService.EntryNotFound
        }
      }
    }

    "FindProperties" must {
      "find properties" in {
        val c1 = Property(tenantId = Some("tenant1"), key = "a", value = "value1")
        val c2 = Property(tenantId = Some("tenant2"), key = "a", value = "value1")
        val c3 = Property(tenantId = Some("tenant1"), key = "b", value = "value1")
        val c4 = Property(tenantId = None, key = "b", value = "value1")
        val actor = newPropertyActor()
        for {
          cc1 <- ask(actor, PropertyService.SetPropertyCmd(c1))
          cc2 <- ask(actor, PropertyService.SetPropertyCmd(c2))
          cc3 <- ask(actor, PropertyService.SetPropertyCmd(c3))
          cc4 <- ask(actor, PropertyService.SetPropertyCmd(c4))
          ccs1 <- ask(actor, PropertyService.FindProperties(tenantId = AnyValue, key = Value("a"))).mapTo[PropertyService.PropertySeq].map(_.entries)
          ccs2 <- ask(actor, PropertyService.FindProperties(tenantId = NoValue, key = Value("b"))).mapTo[PropertyService.PropertySeq].map(_.entries)
          ccs3 <- ask(actor, PropertyService.FindProperties(tenantId = AnyValue, key = Value("b"))).mapTo[PropertyService.PropertySeq].map(_.entries)
          ccs4 <- ask(actor, PropertyService.FindProperties(tenantId = Value("tenant1"))).mapTo[PropertyService.PropertySeq].map(_.entries)
        } yield {
          cc1 shouldBe Done
          cc2 shouldBe Done
          cc3 shouldBe Done
          cc4 shouldBe Done
          println(s"ccs1 = $ccs1")
          println(s"ccs2 = $ccs2")
          println(s"ccs3 = $ccs3")
          ccs1 should contain(c1)
          ccs1 should contain(c2)
          ccs2 should contain(c4)
          ccs3 should contain(c3)
          ccs3 should contain(c4)
          ccs4 should contain(c1)
          ccs4 should contain(c3)
        }
      }
    }
  }
}