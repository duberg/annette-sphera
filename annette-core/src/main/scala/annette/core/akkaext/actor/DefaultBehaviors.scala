package annette.core.akkaext.actor

import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.{ AskSupport, PipeToSupport }
import akka.util.Timeout
import annette.core.akkaext.actor.CqrsQuery._
import annette.core.akkaext.actor.CqrsResponse.{ ChildOpt, Pong, Success, Terminated }

import scala.concurrent.{ ExecutionContext, Future }

trait DefaultBehaviors[State <: CqrsState] extends PipeToSupport with AskSupport { _: Actor =>
  protected var terminateOpt: Option[(ActorRef, Any)] = None

  def changeState(state: State): Unit

  def throwableBehavior: Receive = { case e: Exception => throw e }

  def creatorBehavior: Receive = {
    case p: Props => sender() ! context.actorOf(p)
    case (props: Props, name: String) => sender() ! context.actorOf(props, name)
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
  def exceptionHandler: PartialFunction[Throwable, Unit] = {
    case e: Throwable => self ! e
  }
  def replySuccess(): Unit = if (sender() != self) sender() ! Success

  /**
   * PipeTo self command after Future result.
   */
  def after[R](f: Future[R], cmd: CqrsCommand)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f map { response => cmd } recover exceptionHandler pipeTo self
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

  case class AfterCmd[T](result: T, handler: (T, State) => Unit) extends CqrsCommand
  case class AfterNoReplyCmd[T](result: T, handler: (T, State) => Unit) extends CqrsCommand
  /**
   * Run handler after Future result.
   *
   * Must send sender() ! reply in handler.
   */
  def after[R](f: Future[R])(handler: (R, State) => Unit)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f flatMap { response => self.ask(AfterCmd(response, handler)) } recover exceptionHandler pipeTo sender()
  }

  /**
   * Run handler after Future result.
   *
   * Will not reply to sender() in handler block.
   */
  def afterNoReply[R](f: Future[R])(handler: (R, State) => Unit)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f flatMap { response => self.ask(AfterNoReplyCmd(response, handler)) } recover exceptionHandler
  }

}