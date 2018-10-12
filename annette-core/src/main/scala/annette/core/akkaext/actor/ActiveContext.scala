package annette.core.akkaext.actor

trait ActiveContext[A <: CqrsState] { _: CqrsActor[A] =>
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
