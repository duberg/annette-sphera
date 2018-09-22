package annette.imc.report

import akka.actor.ActorRef
import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.util.{ ByteString, Timeout }
import annette.core.CoreModule
import annette.core.domain.tenancy.model.User
import annette.imc.model.Ap
import annette.imc.report.model.Templates._
import annette.imc.report.model._
import annette.imc.report.process._
import annette.imc.report.retrieve._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class ReportService(
  val coreModule: CoreModule,
  val apsActor: ActorRef,
  val imcUserActor: ActorRef,
  implicit val t: Timeout = 3 minutes)(implicit val c: ExecutionContext)
  extends TemplateDataRetriever with TemplateProcessor {

  /**
   * Генерирование отчета
   *
   * @param id идентификатор шаблона
   * @param apId идентификатор заявки
   * @param expertId идентификатор эксперта
   * @param parameters параметры передаваемые по rest api
   * @param formatType формат выходного отчета
   * @param language язык отчета
   * @return Source
   */
  def generate(
    id: Report.Id,
    apId: Ap.Id,
    expertId: User.Id,
    parameters: Map[String, Any],
    formatType: ReportFormatType,
    language: String): Future[Source[ByteString, Future[IOResult]]] =
    for {
      x <- retrieve(id, apId, expertId, language)
      y <- process(id, x, parameters, formatType, language)
    } yield y

  /**
   * Генерирование отчета без использование механизма извлечения данных (Для тестов)
   *
   * @param id идентификатор шаблона
   * @param apId идентификатор заявки
   * @param expertId идентификатор эксперта
   * @param parameters параметры передаваемые по rest api
   * @param formatType формат выходного отчета
   * @param language язык отчета
   * @return Source
   */
  def generateWithTemplateData(
    id: Report.Id,
    apId: Ap.Id,
    path: String,
    expertId: User.Id,
    parameters: Map[String, Any],
    formatType: ReportFormatType,
    templateData: TemplateData,
    language: String): Future[Unit] =
    process(id, path, templateData, parameters, formatType, language)

  /**
   * Получение информации о списке шаблонов
   *
   * @return
   */
  def getInfoAll: Future[Seq[ReportInfo]] =
    Future(predefined.values.toSeq)
}

