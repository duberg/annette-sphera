package annette.core.security.authorization

object AuthorizationActions extends Enumeration {
  type AuthorizationActions = String

  val POST = "POST"
  val GET = "GET"
  val UPDATE = "UPDATE"
  val PATCH = "PATCH"
  val DELETE = "DELETE"
}
