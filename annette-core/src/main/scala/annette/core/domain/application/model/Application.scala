package annette.core.domain.application.model

/**
 * Created by valery on 16.12.16.
 */
case class Application(
  name: String,
  code: String,
  id: Application.Id)

object Application {
  type Id = String
}

case class ApplicationUpdate(
  name: Option[String],
  code: Option[String],
  id: Application.Id)

