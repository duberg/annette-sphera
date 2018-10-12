package annette.core.domain.tenancy.model

import java.time.LocalDateTime
import java.util.UUID

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language

/**
 * Содержит параметры открытой сессии пользователя
 *
 * @param userId          пользователь сессии
 * @param tenantId        организация сессии
 * @param languageId      язык сессии
 * @param startTimestamp  дата и время начала сессии
 * @param lastOpTimestamp дата и время последней операции
 * @param timeout         Таймаут сессии в минутах. Если таймаут равен 0, то сессия открыта неограниченное время.
 * @param ip              IP адрес пользователя
 * @param timestamp       временная метка записи
 * @param id              уникальный идентификатор открытой сессии
 */
case class OpenSession(
  userId: User.Id,
  tenantId: Tenant.Id,
  applicationId: Application.Id,
  languageId: Language.Id,
  startTimestamp: LocalDateTime,
  lastOpTimestamp: LocalDateTime,
  rememberMe: Boolean,
  timeout: Int,
  ip: String,
  timestamp: LocalDateTime = LocalDateTime.now(),
  id: OpenSession.Id = UUID.randomUUID())

case class PaginateOpenSessions(items: List[OpenSession], totalCount: Int)

case class OpenSessionUpdate(
  id: OpenSession.Id,
  tenantId: Option[Tenant.Id] = None,
  applicationId: Option[Application.Id] = None,
  languageId: Option[Language.Id] = None,
  rememberMe: Option[Boolean] = None,
  lastOpTimestamp: Option[LocalDateTime] = None,
  timestamp: LocalDateTime = LocalDateTime.now())

object OpenSession {
  type Id = UUID
}