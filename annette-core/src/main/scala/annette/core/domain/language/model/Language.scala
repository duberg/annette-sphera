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

package annette.core.domain.language.model

/**
 * Created by valery on 16.12.16.
 */
case class Language(
  name: String,
  id: Language.Id)

object Language {
  type Id = String
}

case class LanguageUpdate(
  name: Option[String],
  id: Language.Id)
