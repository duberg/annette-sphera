package annette.imc.report.model

import java.io.InputStream
import java.util.UUID

object Templates {
  val predefinedIdA: UUID = UUID.fromString("8e041b6a-9ded-11e7-abc4-cec278b6b50a")
  val predefinedIdB: UUID = UUID.fromString("0689ce44-391e-4ac0-bc67-bf5b36bf69de")
  val predefinedIdC: UUID = UUID.fromString("698d7b29-c0be-46fe-b902-89b8b7ef4dcc")
  val predefinedIdD: UUID = UUID.fromString("827e06f9-5898-4291-9c90-482d06dd1f15")
  val predefinedIdE: UUID = UUID.fromString("301a7259-daf9-41e6-b016-b7583ecf3f87")

  val predefined: Map[Report.Id, ReportInfo] = Map(
    predefinedIdA -> ReportInfo(
      id = predefinedIdA,
      name = "Протокол заседания",
      description = Option("Протокол заседания Экспертного совета Фонда международного медицинского кластера"),
      fileNameRu = "templateA.docx"
    ),
    predefinedIdB -> ReportInfo(
      predefinedIdB,
      name = "Заключение",
      description = Option("Заключение Экспертного совета Фонда международного медицинского кластера по заявке"),
      fileNameRu = "templateB.docx"),
    predefinedIdC -> ReportInfo(
      predefinedIdC,
      name = "Бюллетень",
      description = Option(
        "БЮЛЛЕТЕНЬ рассмотрения мероприятий, предлагаемых в заявке претендента, " +
          "на соответствие критериямотбора на получение статуса участника проекта ММК"
      ),
      fileNameRu = "templateRuC.docx",
      fileNameEn = Option("templateEnC.docx")),
    predefinedIdD -> ReportInfo(
      predefinedIdD,
      name = "Бюллетень отказ",
      description = Option(
        "БЮЛЛЕТЕНЬ рассмотрения мероприятий, предлагаемых в заявке претендента, " +
          "на соответствие критериямотбора на получение статуса участника проекта ММК"
      ),
      fileNameRu = "templateRuD.docx",
      fileNameEn = Option("templateEnD.docx")),
    predefinedIdE -> ReportInfo(
      predefinedIdE,
      name = "ПРОТОКОЛ",
      description = Option(
        "ПРОТОКОЛ заседания Экспертного совета Фонда международного медицинского кластера," +
          " проведенного в заочной форме"
      ),
      fileNameRu = "templateE.docx"),
  )

  def getPredefined(id: Report.Id, language: String): Option[ReportInfo] = predefined.get(id)

  def getPredefinedAsStream(id: Report.Id, language: String): InputStream = {
    val x = predefined(id)
    val f = language match {
      case "EN" => x.fileNameEn.getOrElse(x.fileNameRu)
      case _ => x.fileNameRu
    }
    getClass.getResource(s"/templates/$f").openStream
  }
}