

package annette.core.domain.language.model

/**
 * Created by valery on 16.12.16.
 */
case class Language(
  name: String,
  id: Language.Id)

object Language {
  type Id = String
}

case class LanguageUpdate(
  name: Option[String],
  id: Language.Id)
