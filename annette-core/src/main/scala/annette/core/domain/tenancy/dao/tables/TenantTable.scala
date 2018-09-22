/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.tenancy.dao.tables

import annette.core.domain.tenancy.model.Tenant
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class TenantTable extends Table[TenantTable, Tenant] with RootConnector {

  object name extends StringColumn
  object applicationId extends StringColumn
  object applications extends SetColumn[String]
  object languageId extends StringColumn
  object languages extends SetColumn[String]
  object id extends StringColumn with PartitionKey

  override def fromRow(row: Row): Tenant = {
    Tenant(name(row), applicationId(row), applications(row), languageId(row), languages(row), id(row))
  }

  override def tableName: String = "core_tenants"

  def store(entity: Tenant): Future[ResultSet] = {
    insert
      .value(_.id, entity.id)
      .value(_.name, entity.name)
      .value(_.applicationId, entity.applicationId)
      .value(_.applications, entity.applications)
      .value(_.languageId, entity.languageId)
      .value(_.languages, entity.languages)
      .future()
  }

  def getById(id: Tenant.Id): Future[Option[Tenant]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: Tenant.Id): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def isExist(id: Tenant.Id): Future[Boolean] = {
    select(_.id).where(_.id eqs id).one().map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
