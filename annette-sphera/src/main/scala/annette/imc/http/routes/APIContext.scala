package annette.imc.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directive1
import akka.stream.ActorMaterializer
import akka.util.Timeout
import annette.core.CoreModule
import annette.core.services.authentication.SessionData
import annette.imc.ImcContext
import annette.imc.notification.NotificationService

import scala.concurrent.duration._

trait APIContext {
  val coreModule: CoreModule
  val ctx: ImcContext
  val auth: Directive1[SessionData]
  val imcUserActor: ActorRef
  val apsActor: ActorRef
  val notificationService: NotificationService
  implicit val materializer: ActorMaterializer

  implicit lazy val s = ctx.system
  implicit lazy val ec = ctx.system.dispatcher
  implicit val serviceTimeout: Timeout = 30.seconds
}
