package annette.core.test

import java.util.concurrent.{ ExecutorService, Executors }

import akka.actor.{ ActorRef, Props }
import akka.pattern.{ AskSupport, PipeToSupport }
import akka.testkit.{ TestKit, TestProbe }
import akka.util.Timeout
import annette.core.utils.Generator
import org.scalatest.{ AsyncWordSpecLike, Matchers }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Base trait for idiomatic Persistence tests.
 */
trait PersistenceSpec extends AsyncWordSpecLike
  with Generator
  with Matchers
  with ShutdownAfterAll
  with InMemoryCleanup
  with AfterWords
  with PersistenceUtils
  with AskSupport
  with PipeToSupport { _: TestKit =>
  implicit def log = system.log
  implicit val t: Timeout = 5 minute

  override implicit val executionContext: ExecutionContext = new ExecutionContext {
    val threadPool: ExecutorService = Executors.newFixedThreadPool(50)
    def execute(runnable: Runnable): Unit = threadPool.submit(runnable)
    def reportFailure(t: Throwable) {}
  }

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