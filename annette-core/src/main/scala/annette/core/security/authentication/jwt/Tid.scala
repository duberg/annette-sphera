/**
 * *************************************************************************************
 * Copyright (c) 2014-2017 by Valery Lobachev
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted without written permission from Valery Lobachev.
 *
 * Copyright (c) 2014-2017 Валерий Лобачев
 * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
 * запрещено без письменного разрешения правообладателя.
 * **************************************************************************************
 */
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
