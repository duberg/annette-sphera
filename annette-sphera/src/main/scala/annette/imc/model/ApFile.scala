package annette.imc.model

import java.util.UUID

case class ApFile(
  id: UUID,
  name: String,
  lang: String = "Ru", // может еще быть "En"
  fileType: ApFile.FileType,
  comments: String = "")

object ApFile {

  type Id = UUID

  case class FileType(name: String)
  object FileType {
    val ApplicationFile = "ApplicationFile"
    val ChartFile = "ChartFile"
    val ConstituentFiles = "ConstituentFiles"
    val LicencesFiles = "LicencesFiles"
    val QualificationFiles = "QualificationFiles"
    val RegistrationFiles = "RegistrationFiles"
    val Fl160Files = "Fl160Files"
    val OtherFiles = "OtherFiles"
    val BoardDecision = "BoardDecision"
    val ReferenceApplicant = "ReferenceApplicant"
    val Scientific = "SciActivity"
    val Educational = "EduActivity"
  }

}

