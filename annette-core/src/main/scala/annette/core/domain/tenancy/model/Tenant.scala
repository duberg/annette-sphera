package annette.core.domain.tenancy.model

import annette.core.domain.application.model.Application
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
  name: String,
  defaultApplicationId: Application.Id,
  applications: Set[Application.Id],
  defaultLanguageId: Language.Id,
  languages: Set[Language.Id],
  id: Tenant.Id)

object Tenant {
  type Id = String
}

