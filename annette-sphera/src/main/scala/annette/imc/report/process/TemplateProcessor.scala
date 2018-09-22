package annette.imc.report.process

import java.io._

import akka.stream.IOResult
import akka.stream.scaladsl.{ Source, StreamConverters }
import akka.util.ByteString
import annette.imc.report.model.Templates._
import annette.imc.report.model._
import annette.imc.report.poi.Poi._
import fr.opensagres.poi.xwpf.converter.pdf.{ PdfOptions, _ }
import resource._

import scala.concurrent.{ ExecutionContext, Future }

trait TemplateProcessor {
  implicit def c: ExecutionContext
  def process(
    id: Report.Id,
    data: TemplateData,
    parameters: Map[String, Any],
    formatType: ReportFormatType,
    language: String): Future[Source[ByteString, Future[IOResult]]] =
    (for {
      x <- managed(getPredefinedAsStream(id, language))
      y <- managed(new ByteArrayOutputStream).map(processTemplate(id, x, _, data, parameters, formatType))
      z <- managed(new ByteArrayInputStream(y.toByteArray))
    } yield StreamConverters.fromInputStream(() => z)).toFuture

  def process(
    id: Report.Id,
    path: String,
    data: TemplateData,
    parameters: Map[String, Any],
    formatType: ReportFormatType,
    language: String): Future[Unit] =
    (for {
      x <- managed(getPredefinedAsStream(id, language))
      y <- managed(new ByteArrayOutputStream).map(processTemplate(id, x, _, data, parameters, formatType))
    } yield y.writeTo(new FileOutputStream(path))).toFuture

  def processTemplate(
    id: Report.Id,
    in: InputStream,
    out: ByteArrayOutputStream,
    data: TemplateData,
    parameters: Map[String, Any],
    formatType: ReportFormatType): ByteArrayOutputStream = {
    val d = new Document(in)
    (id, data) match {
      case (`predefinedIdA`, data: TemplateDataA) => TemplateProcessorA.process(d, data, parameters)
      case (`predefinedIdB`, data: TemplateDataB) => TemplateProcessorB.process(d, data, parameters)
      case (`predefinedIdC` | `predefinedIdD` | `predefinedIdE`, data: TemplateDataC) => TemplateProcessorC.process(d, data, parameters)
      case _ => sys.error("Invalid template")
    }

    formatType match {
      case ReportFormatType.Word =>
        d.write(out)
      case ReportFormatType.Pdf =>
        val options = PdfOptions.create
        PdfConverter.getInstance().convert(d, out, options)
    }

    out
  }
}