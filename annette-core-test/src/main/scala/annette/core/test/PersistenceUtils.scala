package annette.core.test

import akka.actor.ActorRef
import akka.pattern.ask
import akka.testkit.{ DefaultTimeout, TestKit }
import annette.core.persistence.Persistence._

import scala.concurrent.Future

trait PersistenceUtils { _: TestKit with DefaultTimeout =>
  import system.dispatcher

  def ping(x: ActorRef): Future[Any] = ask(x, Ping)

  def pingAll(x: Iterable[ActorRef]): Future[Seq[Any]] =
    Future.sequence(for (i <- x.toSeq) yield ping(i))

  def kill(a: ActorRef): Future[Any] = ask(a, Kill)

  def waitSome: Future[Unit] = Future(Thread.sleep(100))
}
