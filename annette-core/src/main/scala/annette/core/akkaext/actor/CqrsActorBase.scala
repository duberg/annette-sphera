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
trait CqrsActorBase[A <: CqrsState] extends Actor
  with ActorLogging
  with AskSupport
  with PipeToSupport
  with Generator
  with DefaultBehaviors[A] {

  val parent: ActorRef = context.parent

  /**
   * Actor Id
   */
  val id: String = context.self.path.toStringWithoutAddress

  /**
   * Init actor state
   */
  def initState: A

  def activeContext(state: A): Receive

  /**
   * Поведение актора которое необходимо реализовать.
   */
  def behavior(state: A): Receive

  /**
   * Поведение актора которое подмешано перед основным [[behavior]]
   */
  def preBehavior(state: A): Receive = PartialFunction.empty

  /**
   * Поведение актора которое подмешано после основного [[behavior]]
   */
  def postBehavior(state: A): Receive = PartialFunction.empty

  def orElseBehavior(state: A): Receive = PartialFunction.empty

  def defaultBehavior(state: A): Receive = {
    preBehavior(state)
      .orElse(behavior(state))
      .orElse(postBehavior(state))
      .orElse(orElseBehavior(state))
  }

  /**
   * Change actor state.
   */
  def changeState(state: A): Unit = context.become(activeContext(state))

  def publish(event: CqrsEvent): Unit = context.system.eventStream.publish(event)
}
