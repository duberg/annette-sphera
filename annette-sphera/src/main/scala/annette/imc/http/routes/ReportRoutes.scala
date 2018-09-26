package annette.imc.http.routes

import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import annette.imc.report.ReportService

import scala.util.{ Failure, Success }
import CacheDirectives._
import ContentDispositionTypes._
import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.util.ByteString
import annette.core.AnnetteException
import annette.core.utils.Generator
import annette.imc.report.model.ReportFormatType

import scala.concurrent.Future

trait ReportRoutes
  extends Generator { self: APIContext with API =>

  val reportService: ReportService
  def reportRoutes: Route = {
    pathPrefix("report") {
      (path("get-info-all") & get & auth) { sessionData =>
        onSuccess(reportService.getInfoAll)(complete(_))
      } ~
        // Route: /imc/api/report/generate/apId/reportId?format=word
        // Route: /imc/api/report/generate/apId/reportId?format=pdf
        (path("generate" / JavaUUID / JavaUUID) & get & auth) { (apId, reportId, sessionData) =>
          parameter('format.?) { x =>
            val userId = sessionData.userId
            val format = ReportFormatType(x)
            val f = for {
              user <- coreModule.userManager.getById(userId).map(_.get)
              x <- reportService.generate(reportId, apId, userId, Map.empty, format, user.language.getOrElse(""))
            } yield x
            onComplete(f) {
              case Success(source: Source[ByteString, Future[IOResult]]) =>
                complete(HttpResponse(
                  entity = HttpEntity(MediaTypes.`application/msword`, source),
                  headers = List(
                    `Cache-Control`(`no-cache`),
                    `Content-Disposition`.apply(attachment, Map("filename" -> generateFileName(format.fileExtension))))))
              case Failure(throwable) =>
                throwable match {
                  case annetteException: AnnetteException =>
                    complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                  case _ =>
                    complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
                }
            }
          }
        } ~
        // Route: /imc/api/report/generate/apId/reportId?format=word
        // Route: /imc/api/report/generate/apId/reportId?format=pdf
        (path("generate" / JavaUUID / JavaUUID) & post & auth & entity(as[Map[String, String]])) { (apId, reportId, sessionData, parameters) =>
          parameter('format.?) { x =>
            val userId = sessionData.userId
            val format = ReportFormatType(x)
            val f = for {
              user <- coreModule.userManager.getById(userId).map(_.get)
              x <- reportService.generate(reportId, apId, userId, parameters, format, user.language.getOrElse(""))
            } yield x
            onComplete(f) {
              case Success(source) =>
                complete(HttpResponse(
                  entity = HttpEntity(MediaTypes.`application/msword`, source),
                  headers = List(
                    `Cache-Control`(`no-cache`),
                    `Content-Disposition`.apply(attachment, Map("filename" -> generateFileName(format.fileExtension))))))
              case Failure(throwable) =>
                throwable match {
                  case annetteException: AnnetteException =>
                    complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                  case _ =>
                    complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
                }
            }
          }
        } ~
        // Route: /imc/api/report/bulletin-for-expert/reportId/apId/expertId
        (path("bulletin-for-expert" / JavaUUID / JavaUUID / JavaUUID) & get & auth) { (reportId, apId, expertId, sessionData) =>
          val userId = sessionData.userId
          val format = ReportFormatType("pdf")
          val f = for {
            user <- coreModule.userManager.getById(expertId).map(_.get)
            x <- reportService.generate(reportId, apId, expertId, Map.empty, format, user.language.getOrElse(""))
          } yield x
          onComplete(f) {
            case Success(source: Source[ByteString, Future[IOResult]]) =>
              complete(HttpResponse(
                entity = HttpEntity(MediaTypes.`application/msword`, source),
                headers = List(
                  `Cache-Control`(`no-cache`),
                  `Content-Disposition`.apply(attachment, Map("filename" -> generateFileName(format.fileExtension))))))
            case Failure(throwable) =>
              throwable match {
                case annetteException: AnnetteException =>
                  complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
                case _ =>
                  complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
              }
          }
        }
    }
  }
}
