package annette.core.security.authentication

import annette.core.domain.application._
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ OpenSession, Tenant, User }

case class Session(
  sessionId: OpenSession.Id,
  userId: User.Id,
  tenantId: Tenant.Id,
  applicationId: Application.Id,
  languageId: Language.Id)
