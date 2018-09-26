package annette.core.akkaext.actor

import akka.util.Timeout

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Functional actor
 */
trait CqrsActorLike extends CqrsActorBase {
  def receiveContext(state: State): Receive =
    defaultBehavior(state)
      .orElse(afterBehavior(state))
      .orElse(getStateBehavior(state))
      .orElse(updateStateBehavior(state))
      .orElse(creatorBehavior)
      .orElse(throwableBehavior)
      .orElse(echoBehavior)
      .orElse(terminateBehavior)
      .orElse(notMatchedBehavior)

  def receive: Receive = receiveContext(initState)
}

trait CqrsActor[S <: CqrsState] extends CqrsActorLike {
  type State = S
}