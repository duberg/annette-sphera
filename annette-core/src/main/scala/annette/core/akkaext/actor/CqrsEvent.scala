package annette.core.akkaext.actor

trait CqrsEvent

object CqrsEvent {
  case class InitializedEvt(state: AnyRef) extends CqrsEvent
}
