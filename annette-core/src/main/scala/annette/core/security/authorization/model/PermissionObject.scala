package annette.core.domain.authorization.model

/**
 * Created by kantemirov on 27.02.17.
 */
case class PermissionObject(
  permissionId: Permission.Id,
  keys: Set[String])
