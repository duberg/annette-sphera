package annette.imc.model

case class OperationType(nameMessage: String, details: Option[ApString] = None) {
  def getMedType(): String = OperationType.getMedType(nameMessage)

}

object OperationType {
  //  private val root = "imc.ap.operationtype."
  private val root = ""

  //Диагностическая и амбулаторная клиника
  val CLINIC: String = root + "clinic"

  ///Амбулаторная клиника
  val DISPENCERY: String = root + "dispensary"

  ///Диагностическая клиника
  val DIAGNOSTIC: String = root + "diagnostic"

  //Сердечно-сосудистые заболевания
  val CARDIO: String = root + "cardio"

  //Онкология
  val ONCOLOGY: String = root + "oncology"

  //Клиника травматологии, ортопедии и эндопротезирования
  val TRAUMA: String = root + "trauma"

  //Неврология
  val NEUROLOGY: String = root + "neurology"

  //Акушерство и Гинекология
  val OBSTETRICS: String = root + "obstetrics"

  //Урология
  val UROLOGY: String = root + "urology"

  //Реабилитация
  val REHAB: String = root + "rehab"

  //Глазная хирургия
  val SURGERY: String = root + "surgery"

  //Другое (медицина)
  val OTHERMED: String = root + "othermed"

  //Научные разработки в здравоохранении
  val SCIENCE: String = root + "science"

  //Для врачей
  val FORDOCTORS: String = root + "fordoctors"

  //Для медицинских сестер
  val FORNURSES: String = root + "fornurses"

  //Для менеджеров в сфере здравоохранения
  val FORMANAGERS: String = root + "formanagers"

  // Другое (образование)
  val OTHERED: String = root + "othered"

  // Другая деятельность
  val OTHER: String = root + "other"

  def getMedType(message: String): String = {
    val types = Map(
      CLINIC -> "Medicine",
      CARDIO -> "Medicine",
      ONCOLOGY -> "Medicine",
      DISPENCERY -> "Medicine",
      DIAGNOSTIC -> "Medicine",
      TRAUMA -> "Medicine",
      NEUROLOGY -> "Medicine",
      OBSTETRICS -> "Medicine",
      UROLOGY -> "Medicine",
      REHAB -> "Medicine",
      SURGERY -> "Medicine",
      SCIENCE -> "Science",
      FORDOCTORS -> "Education",
      FORNURSES -> "Education",
      FORMANAGERS -> "Education",
      OTHERED -> "Education",
      OTHER -> "Other")
    types.getOrElse(message, "Medicine")
  }

  def make(shortName: String, details: Option[ApString] = None): OperationType =
    OperationType(root + shortName, details)
}
