/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.application.model

/**
 * Created by valery on 16.12.16.
 */
case class Application(
  name: String,
  code: String,
  id: Application.Id)

object Application {
  type Id = String
}

case class ApplicationUpdate(
  name: Option[String],
  code: Option[String],
  id: Application.Id)

