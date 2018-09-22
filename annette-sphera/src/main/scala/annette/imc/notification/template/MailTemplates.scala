package annette.imc.notification.template

import resource._

import scala.io.Source

/**
 * Шаблоны почтовых сообщений.
 *
 * todo: Нужно поместить все это в хранилице и добавить динамику,
 * чтобы пользователь мог редактировать шаблоны
 */
object MailTemplates {
  val SubjectEn = "Moscow International Medical Cluster"
  val SubjectRu = "Международный медицинский кластер"

  def subject(language: String): String = language match {
    case "EN" => SubjectEn
    case _ => SubjectRu
  }

  def password(language: String, password: String, parameters: Map[String, String]): String = {
    val p = parameters ++ Map(
      "Subject" -> SubjectRu,
      "Password" -> password)
    processTemplate("A", language, p)
  }

  def toExpertise(language: String, parameters: Map[String, String]): String = {
    val p = parameters ++ Map(
      "Subject" -> SubjectRu)
    processTemplate("B", language, p)
  }

  def toReview(language: String, parameters: Map[String, String]): String = {
    val p = parameters ++ Map(
      "Subject" -> SubjectRu)
    processTemplate("F", language, p)
  }

  def notReady(language: String, parameters: Map[String, String]): String = {
    val p = parameters ++ Map(
      "Subject" -> SubjectRu)
    processTemplate("E", "RU", p)
  }

  def processTemplate(name: String, language: String, parameters: Map[String, String]): String =
    (for (stream <- managed(getClass.getResource(s"/templates/template${language.toLowerCase.capitalize}$name.html").openStream)) yield {
      val x = Source.fromInputStream(stream).mkString
      (x /: parameters) {
        case (str, (n, v)) => str.replace(s"{{$n}}", v)
      }
    }).opt.get
}