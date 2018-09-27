package annette.core.akkaext.actor

import akka.util.Timeout

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Functional actor
 */
trait CqrsActor[A <: CqrsState] extends CqrsActorBase[A] {
  def activeContext(state: A): Receive =
    defaultBehavior(state)
      .orElse(afterBehavior(state))
      .orElse(getStateBehavior(state))
      .orElse(updateStateBehavior(state))
      .orElse(creatorBehavior)
      .orElse(throwableBehavior)
      .orElse(echoBehavior)
      .orElse(terminateBehavior)
      .orElse(notMatchedBehavior)

  def receive: Receive = activeContext(initState)
}