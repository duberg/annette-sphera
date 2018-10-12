package annette.core.akkaext.actor

import akka.actor.Props

trait CqrsQuery

object CqrsQuery {
  case object Ping extends CqrsQuery
  case object Kill extends CqrsQuery
  case class CreateChild(x: Props, y: String) extends CqrsQuery
  case class Publish(event: CqrsEvent) extends CqrsQuery
  case object GetState extends CqrsQuery
}