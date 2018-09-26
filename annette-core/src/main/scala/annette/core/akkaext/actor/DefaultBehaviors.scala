package annette.core.akkaext.actor

import akka.actor.{ ActorRef, Props }
import annette.core.akkaext.actor.CqrsQuery._
import annette.core.akkaext.actor.CqrsResponse.{ ChildOpt, Pong, Success, Terminated }

trait DefaultBehaviors { _: CqrsActorBase =>
  protected var terminateOpt: Option[(ActorRef, Any)] = None

  def throwableBehavior: Receive = { case e: Exception => throw e }

  def creatorBehavior: Receive = {
    case p: Props => sender() ! context.actorOf(p)
    case (props: Props, name: String) => sender() ! context.actorOf(props, name)
    case GetChild(childId) => sender() ! ChildOpt(getChildOpt(childId))
  }

  def getStateBehavior(state: State): Receive = {
    case GetState => sender() ! state
  }

  def updateStateBehavior(state: State): Receive = {
    case evt: CqrsEvent =>
      changeState(state.updated(evt))
      sender() ! Success
  }

  def echoBehavior: Receive = {
    case "ping" => sender() ! "pong"
    case Ping => sender() ! Pong
  }

  /**
   * Поведение: Правильное завершение актора.
   *
   * Необходимо использовать в тестах.
   */
  def terminateBehavior: Receive = {
    case "kill" =>
      context.stop(self)
      terminateOpt = Option(sender() -> "terminated")
    case Kill =>
      context.stop(self)
      terminateOpt = Option(sender() -> Terminated)
  }

  /**
   * Поведение: Оповещение о новом [[CqrsEvent]] событии.
   *
   * Незаменимый механизм при тестировании акторов с футурами.
   * Используется для тестирования движка бизнес-процессов.
   */
  def publishBehavior: Receive = {
    case Publish(event) =>
      context.system.eventStream.publish(event)
      replySuccess()
  }

  def notMatchedBehavior: Receive = {
    case _ =>
  }

  def afterBehavior(state: State): Receive = {
    case x: AfterCmd[_] =>
      x.handler(x.result, state)
    case x: AfterNoReplyCmd[_] =>
      x.handler(x.result, state)
      sender() ! Success
  }
}