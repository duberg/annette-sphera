package annette.core.domain.tenancy.model

import java.util.UUID

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import org.joda.time.DateTime

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
  startTimestamp: DateTime,
  lastOpTimestamp: DateTime,
  rememberMe: Boolean,
  timeout: Int,
  ip: String,
  timestamp: DateTime = DateTime.now(),
  id: OpenSession.Id = UUID.randomUUID())

object OpenSession {
  type Id = UUID
}

case class OpenSessionUpdate(
  id: OpenSession.Id,
  tenantId: Option[Tenant.Id] = None,
  applicationId: Option[Application.Id] = None,
  languageId: Option[Language.Id] = None,
  rememberMe: Option[Boolean] = None,
  lastOpTimestamp: Option[DateTime] = None,
  timestamp: DateTime = DateTime.now())

