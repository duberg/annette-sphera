package annette.core.security.authentication

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ Tenant, TenantData, User }

case class ApplicationState(
  authenticated: Boolean,
  language: Language,
  languages: Seq[Language],
  user: Option[User] = None,
  tenant: Option[Tenant] = None,
  application: Option[Application] = None,
  tenantData: Set[TenantData] = Set.empty,
  jwtToken: Option[String] = None)
