
package annette.core.security.authentication.jwt

import java.util.UUID

import io.igl.jwt.{ ClaimField, ClaimValue }
import play.api.libs.json.{ JsString, JsValue }

case class Uid(value: UUID) extends ClaimValue {

  // A reference to the field object
  override val field: ClaimField = Uid

  // A json representation of our value
  override val jsValue: JsValue = JsString(value.toString)
}

object Uid extends (UUID => Uid) with ClaimField {

  // A function that attempts to construct our claim from a json value
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[UUID].map(apply)

  // The field name
  override val name: String = "Uid"
}
