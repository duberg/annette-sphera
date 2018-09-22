package annette.core.test

import akka.actor.{ ActorRef, Props }
import akka.testkit.{ DefaultTimeout, TestKit, TestProbe }
import annette.core.utils.Generator
import org.scalatest.{ AsyncWordSpecLike, Matchers }

import scala.concurrent.Future

/**
 * Base trait for idiomatic Persistence tests.
 */
trait PersistenceSpec extends AsyncWordSpecLike
  with Generator
  with DefaultTimeout
  with Matchers
  with ShutdownAfterAll
  with InMemoryCleanup
  with AfterWords
  with PersistenceUtils { _: TestKit =>
  /**
   * Create new TestProbe.
   */
  def newTestProbe = new TestProbe(system, generateId)

  /**
   * Create new TestProbe ActorRef.
   */
  def newTestProbeRef: ActorRef = new TestProbe(system, generateId).ref

  /**
   * Create child actor in TestProbe context.
   *
   * @param props props
   * @return ActorRef, TestProbe pair
   */
  def createInContext(props: Props, name: String): (TestProbe, ActorRef) = {
    val p = new TestProbe(system, generateId)
    val a = p.childActorOf(props, name)
    (p, a)
  }

  /**
   * Генерация id в контексте фьючера нужна для того чтобы тесты работали правильно.
   * Не использовать без необходимости.
   */
  def generateIdFuture = Future { generateId }
}