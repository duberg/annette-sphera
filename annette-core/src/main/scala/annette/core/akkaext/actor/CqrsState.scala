package annette.core.akkaext.actor

trait CqrsState {
  type Update = PartialFunction[CqrsEvent, CqrsState]
  private type Updated = PartialFunction[CqrsEvent, this.type]

  def update: Update
  def preUpdate: Update = PartialFunction.empty
  def postUpdate: Update = PartialFunction.empty
  def orElseUpdate: Update = PartialFunction.empty

  private def exceptionHandler: Update = { case x => sys.error(s"Unknown event $x when update state") }

  final def updated: Updated = {
    preUpdate
      .orElse(update)
      .orElse(postUpdate)
      .orElse(orElseUpdate)
      .orElse(exceptionHandler)
      .asInstanceOf[Updated]
  }
}

case class EmptyState() extends CqrsState {
  def update = { case _ => this }
}