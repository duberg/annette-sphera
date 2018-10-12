package annette.core.akkaext.actor

/**
 * Functional actor
 */
trait CqrsActor[A <: CqrsState] extends CqrsActorBase[A] with ActiveContext[A] {
  /**
   * Init actor state
   */
  def initState: A

  /**
   * Поведение актора которое необходимо реализовать.
   */
  def behavior(state: A): Receive
}