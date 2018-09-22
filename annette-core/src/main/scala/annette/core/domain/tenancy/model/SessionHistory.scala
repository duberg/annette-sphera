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
import org.joda.time.DateTime

/**
 * Содержит историю пользовательских сессий
 *
 * @param userId         пользователь сессии
 * @param tenantId       организация сессии
 * @param languageId     язык сессии
 * @param startTimestamp дата и время начала сессии
 * @param endTimestamp   дата и время окончания сессии
 * @param ip             IP адрес пользователя
 * @param id             уникальный идентификатор сессии
 */
case class SessionHistory(
  userId: User.Id,
  tenantId: Tenant.Id,
  applicationId: Application.Id,
  languageId: Language.Id,
  startTimestamp: DateTime,
  endTimestamp: DateTime,
  ip: String,
  id: OpenSession.Id)
