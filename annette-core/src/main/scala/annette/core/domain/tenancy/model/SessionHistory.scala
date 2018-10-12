package annette.core.domain.tenancy.model

import java.time.LocalDateTime

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language

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
  startTimestamp: LocalDateTime,
  endTimestamp: LocalDateTime,
  ip: String,
  id: OpenSession.Id)
