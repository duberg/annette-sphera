package annette.core.security.authorization

class ParamValidationUtil {

}

object ParamValidationUtil {

  def validateActions(actions: Seq[String]) = {
    actions foreach {
      action: String =>
        action.toUpperCase() match {
          case AuthorizationActions.PATCH | AuthorizationActions.GET | AuthorizationActions.DELETE | AuthorizationActions.UPDATE | AuthorizationActions.POST =>

          case _ => throw new Exception(s"Not defined action ${action}")
        }
    }
  }

}