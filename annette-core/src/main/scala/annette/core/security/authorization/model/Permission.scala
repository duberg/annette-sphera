package annette.core.domain.authorization.model

/**
 * Created by kantemirov on 04.02.17.
 */
case class Permission(
  id: Permission.Id,
  description: String)

object Permission {
  type Id = String
}
