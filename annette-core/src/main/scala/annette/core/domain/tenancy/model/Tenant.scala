package annette.core.domain.tenancy.model

import annette.core.akkaext.actor.{CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse}
import annette.core.akkaext.http.PageRequest
import annette.core.domain.application.Application
import annette.core.domain.language.model.Language

/**
 * Содержит основные реквизиты организации
 *
 * @param name          Наименование
 * @param defaultApplicationId Приложение по умолчанию
 * @param applications  Приложения присвоенные организации
 * @param defaultLanguageId    Язык по умолчанию
 * @param languages     Языки присвоенные организации
 * @param id            Уникальный идентификатор
 */
case class Tenant(
  id: Tenant.Id,
  name: String,
  defaultApplicationId: Application.Id,
  applications: Set[Application.Id],
  defaultLanguageId: Language.Id,
  languages: Set[Language.Id])

case class PaginateTenantsList(items: List[Tenant], totalCount: Int)

case class CreateTenant(
                       id: Tenant.Id,
                       name: String,
                       defaultApplicationId: Application.Id,
                       applications: Set[Application.Id],
                       defaultLanguageId: Language.Id,
                       languages: Set[Language.Id],
                       )

case class UpdateTenant()

object Tenant {
  type Id = String

  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case class CreateTenantCmd(x: CreateTenant) extends Command
  case class UpdateTenantCmd(x: UpdateTenant) extends Command
  case class DeleteTenantCmd(userId: Tenant.Id) extends Command

  case class GetTenantById(id: Tenant.Id) extends Query
  object ListTenants extends Query
  case class PaginateListTenants(page: PageRequest) extends Query

  case class CreatedTenantEvt(x: Tenant) extends Event
  case class UpdatedTenantEvt(x: UpdateTenant) extends Event
  case class DeletedTenantEvt(userId: Tenant.Id) extends Event

  case class CreateTenantSuccess(x: Tenant) extends Response
  case class TenantOpt(maybeEntry: Option[Tenant]) extends Response
  case class TenantsMap(x: Map[Tenant.Id, Tenant]) extends Response
  case class TenantsList(x: PaginateTenantsList) extends Response
}

