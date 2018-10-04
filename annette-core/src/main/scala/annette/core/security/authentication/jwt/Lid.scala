
package annette.core.security.authentication.jwt

import io.igl.jwt.{ ClaimField, ClaimValue }
import play.api.libs.json.{ JsString, JsValue }

case class Lid(value: String) extends ClaimValue {

  // A reference to the field object
  override val field: ClaimField = Lid

  // A json representation of our value
  override val jsValue: JsValue = JsString(value)
}

object Lid extends (String => Lid) with ClaimField {

  // A function that attempts to construct our claim from a json value
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)

  // The field name
  override val name: String = "Lid"
}