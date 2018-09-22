package annette.imc.report.model

case class TemplateDataA(
  presentRows: Seq[(String, String, String)],
  membersOfTheExpertCouncilRows: Seq[(String, String, String)],
  inviteesRows: Seq[(String, String, String)],
  parameters: Map[String, Any]) extends TemplateData
