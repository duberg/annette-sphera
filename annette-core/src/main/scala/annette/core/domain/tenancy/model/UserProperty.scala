package annette.core.domain.tenancy.model

import java.util.UUID

import annette.core.domain.application.Application

case class UserProperty(
  id: UserProperty.Id,
  value: String)

object UserProperty {
  case class Id(
    userId: User.Id,
    tenantId: Option[Tenant.Id] = None,
    applicationId: Option[Application.Id] = None,
    key: String) {
    override def toString = s"$userId.${tenantId.getOrElse("")}.${applicationId.getOrElse("")}.$key"
  }

  object Id {
    def apply(raw: String): Id = {
      val parts = raw.split(".").toSeq
      val userId = UUID.fromString(parts.head)
      val tenantId = if (parts(1).isEmpty) None else Some(parts(1))
      val applicationId = if (parts(2).isEmpty) None else Some(parts(2))
      val key = parts(3)

      Id(
        userId = userId,
        tenantId = tenantId,
        applicationId = applicationId,
        key = key)
    }
  }

}