package annette.imc.report.model

case class TemplateDataB(
  documentRows: Seq[String],
  commentRows: Seq[(String, String, String)],
  parameters: Map[String, Any]) extends TemplateData
