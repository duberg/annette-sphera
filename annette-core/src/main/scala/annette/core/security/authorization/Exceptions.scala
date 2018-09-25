package annette.core.domain.authorization

import annette.core.AnnetteException

class PermissionAlreadyExists extends AnnetteException("core.authorization.permission.alreadyExists")
class PermissionNotFound extends AnnetteException("core.authorization.permission.notFound")

class RoleAlreadyExists extends AnnetteException("core.authorization.role.alreadyExists")
class RoleNotFound extends AnnetteException("core.authorization.role.notFound")
class RoleAssignedToUser extends AnnetteException("core.authorization.role.assignedToUser")

class UserRoleAlreadyExists extends AnnetteException("core.authorization.userRole.alreadyExists")
class UserRoleNotFound extends AnnetteException("core.authorization.userRole.notFound")
