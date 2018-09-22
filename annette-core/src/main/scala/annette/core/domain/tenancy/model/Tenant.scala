/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.model

import annette.core.domain.application.model.Application
import annette.core.domain.language.model.Language

/**
 * Содержит основные реквизиты организации
 *
 * @param name          Наименование
 * @param applicationId Приложение по умолчанию
 * @param applications  Приложения присвоенные организации
 * @param languageId    Язык по умолчанию
 * @param languages     Языки присвоенные организации
 * @param id            Уникальный идентификатор
 */
case class Tenant(
  name: String,
  applicationId: Application.Id,
  applications: Set[Application.Id],
  languageId: Language.Id,
  languages: Set[Language.Id],
  id: Tenant.Id)

object Tenant {
  type Id = String
}

