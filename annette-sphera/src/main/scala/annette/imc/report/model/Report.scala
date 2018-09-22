package annette.imc.report.model

import java.util.UUID

object Report {
  type Id = UUID
}

case class ReportInfo(
  id: Report.Id,
  name: String,
  description: Option[String],
  fileNameRu: String,
  fileNameEn: Option[String] = None)

trait ReportFormatType {
  def fileExtension: String
}

object ReportFormatType {
  case object Word extends ReportFormatType {
    val fileExtension = "docx"
  }
  case object Pdf extends ReportFormatType {
    val fileExtension = "pdf"
  }

  def apply(v: String): ReportFormatType = v match {
    case "pdf" => ReportFormatType.Pdf
    case _ => ReportFormatType.Word
  }

  def apply(v: Option[String]): ReportFormatType = v match {
    case Some(x) => ReportFormatType(x)
    case None => ReportFormatType.Word
  }
}

