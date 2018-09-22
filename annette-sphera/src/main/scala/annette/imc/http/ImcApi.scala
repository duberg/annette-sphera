package annette.imc.http

import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.http.scaladsl.server.{ Directive1, Route }
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer
import annette.core.CoreModule
import annette.imc.{ ApsActor, ApsState, ApsStorage, ImcContext }
import annette.core.services.authentication.SessionData
import annette.imc.notification._
import annette.imc.report.ReportService
import annette.imc.user.{ ImcUserActor, ImcUserState }
import com.typesafe.config.ConfigFactory
import annette.imc.http.routes._

class ImcApi(val coreModule: CoreModule, ctx1: ImcContext, auth1: Directive1[SessionData]) extends AdminRoutes
  with ApRoutes
  with BulletinRoutes
  with CriterionRoutes
  with FileRoutes
  with NotificationRoutes
  with ReportRoutes
  with StatusRoutes
  with ExpertRoutes
  with APIContext
  with PdfRoute
  with API {

  override lazy val ctx: ImcContext = ctx1
  override lazy val auth: Directive1[SessionData] = auth1

  private val initState: ApsState = ApsState(storage = ApsStorage())
  private val apsActorId = "ApsActor"
  private val imcUserId = "ImcUserActor"

  override val apsActor = ctx.system.actorOf(ApsActor.props(apsActorId, initState))
  override val imcUserActor = ctx.system.actorOf(ImcUserActor.props(imcUserId, ImcUserState()))

  override val notificationService = NotificationService(apsActor, ctx.config)
  override val reportService = new ReportService(coreModule, apsActor, imcUserActor)

  override lazy val materializer = ActorMaterializer()

  def routes: Route = pathPrefix("imc" / "api") {
    implicit val routingSettings: RoutingSettings = RoutingSettings(ConfigFactory.load())

    Route.seal(adminRoutes ~ apRoutes ~ expertRoutes ~ fileRoutes ~ criterionRoutes ~
      statusRoutes ~ bulletinRoutes ~ reportRoutes ~ notificationRoutes ~ pdfRoutes)
  }

}
