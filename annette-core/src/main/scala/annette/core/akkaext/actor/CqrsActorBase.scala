package annette.core.akkaext.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.{ AskSupport, PipeToSupport }
import akka.util.Timeout
import annette.core.akkaext.actor.CqrsQuery.{ GetState, Publish }
import annette.core.akkaext.actor.CqrsResponse.Success
import annette.core.utils.Generator

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Functional actor
 */
trait CqrsActorBase extends Actor
  with ActorLogging
  with AskSupport
  with PipeToSupport
  with Generator
  with DefaultBehaviors {

  type State <: CqrsState

  val parent: ActorRef = context.parent

  def id: ActorId

  def initState: State

  def receiveContext(state: State): Receive

  /**
   * Поведение актора которое необходимо реализовать.
   */
  def behavior(state: State): Receive

  def exceptionHandler: PartialFunction[Throwable, Unit] = {
    case e: Throwable => self ! e
  }

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

  /**
   * PipeTo self command after Future result.
   */
  def after[R](f: Future[R], cmd: CqrsCommand)(implicit c: ExecutionContext, t: Timeout): Unit = {
    f map { response => cmd } recover exceptionHandler pipeTo self
  }

  /**
   * Поведение актора которое подмешано перед основным [[behavior]]
   */
  def preBehavior(state: State): Receive = PartialFunction.empty

  /**
   * Поведение актора которое подмешано после основного [[behavior]]
   */
  def postBehavior(state: State): Receive = PartialFunction.empty

  def orElseBehavior(state: State): Receive = PartialFunction.empty

  def defaultBehavior(state: State): Receive = {
    preBehavior(state)
      .orElse(behavior(state))
      .orElse(postBehavior(state))
      .orElse(orElseBehavior(state))
  }

  /**
   * Change actor state.
   */
  def changeState(state: State): Unit = context.become(receiveContext(state))

  def getChildOpt(childId: ActorId): Option[ActorRef] = context.child(childId.name)

  def getChild(childId: ActorId): ActorRef = getChildOpt(childId).get

  def generateChildId: ActorId = id / generateUUIDStr

  def publish(event: CqrsEvent): Unit = context.system.eventStream.publish(event)

  def replySuccess(): Unit = if (sender() != self) sender() ! Success

  case class AfterCmd[T](result: T, handler: (T, State) => Unit) extends CqrsCommand
  case class AfterNoReplyCmd[T](result: T, handler: (T, State) => Unit) extends CqrsCommand
}
