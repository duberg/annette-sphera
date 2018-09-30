package annette.core.security.authorization

case class AuthrPolicy(roleName: String, accessPath: String, actions: Seq[String])
case class AuthrReqUser(userId: String, accessPath: String, action: String)
case class AuthrUser(userId: String, roleName: String)

class AuthrModel {

}