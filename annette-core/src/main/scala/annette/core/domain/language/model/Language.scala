package annette.core.domain.language.model

case class Language(name: String, id: Language.Id)

case class LanguageUpdate(
  name: Option[String],
  id: Language.Id)

object Language {
  type Id = String
}

