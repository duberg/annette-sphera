package annette.core.security.verification

import akka.event.{ ActorEventBus, EventBus, LookupClassification }
import annette.core.security.verification.Verification.{ EmailVerifiedEvt, VerifiedEvt }
import javax.inject.Singleton

@Singleton
class VerificationBus extends EventBus with LookupClassification with ActorEventBus {
  type Event = Verification.Event
  type Classifier = String

  def mapSize(): Int = 2
  def classify(event: Event): String = event match {
    case x: VerifiedEvt => "verified"
    case x: EmailVerifiedEvt => "email"
  }
  def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}

