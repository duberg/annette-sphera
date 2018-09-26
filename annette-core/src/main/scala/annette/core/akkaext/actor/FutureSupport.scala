package annette.core.akkaext.actor

import akka.pattern.AskSupport
import akka.util.Timeout

import scala.concurrent.ExecutionContext

trait FutureSupport extends AskSupport {
  implicit val c: ExecutionContext
  implicit val t: Timeout
}
