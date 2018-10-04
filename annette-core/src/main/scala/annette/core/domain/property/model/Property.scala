
package annette.core.domain.property.model

import annette.core.domain.application.Application
import annette.core.domain.language.model.Language
import annette.core.domain.tenancy.model.{ Tenant, User }

case class Property(
  userId: Option[User.Id] = None,
  tenantId: Option[Tenant.Id] = None,
  applicationId: Option[Application.Id] = None,
  languageId: Option[Language.Id] = None,
  key: String,
  value: String) {
  val id: Property.Id = Property.id(userId, tenantId, applicationId, languageId, key)
}

object Property {
  type Id = String

  def id(
    userId: Option[User.Id] = None,
    tenantId: Option[Tenant.Id] = None,
    applicationId: Option[Application.Id] = None,
    languageId: Option[Language.Id] = None,
    key: String) = {
    userId.map(_.toString).getOrElse("") + "/" +
      tenantId.map(_.toString).getOrElse("") + "/" +
      applicationId.getOrElse("") + "/" +
      languageId.getOrElse("") + "/" +
      key
  }
}

//userId: Option[User.Id], tenantId: Option[Tenant.Id], applicationId: Option[Application.Id], languageId: Option[Language.Id], key: String

