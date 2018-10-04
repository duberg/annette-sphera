
package annette.core.security.authentication.jwt

import io.igl.jwt.{ ClaimField, ClaimValue }
import play.api.libs.json.{ JsString, JsValue }

case class Tid(value: String) extends ClaimValue {

  // A reference to the field object
  override val field: ClaimField = Tid

  // A json representation of our value
  override val jsValue: JsValue = JsString(value)
}

object Tid extends (String => Tid) with ClaimField {

  // A function that attempts to construct our claim from a json value
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)

  // The field name
  override val name: String = "Tid"
}
