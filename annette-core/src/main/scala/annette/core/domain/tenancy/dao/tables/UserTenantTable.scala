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

import annette.core.domain.tenancy.model.{ Tenant, TenantUser, User }
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class UserTenantTable extends Table[UserTenantTable, TenantUser] with RootConnector {

  object tenantId extends StringColumn with PrimaryKey
  object userId extends UUIDColumn with PartitionKey

  override def fromRow(row: Row): TenantUser = {
    TenantUser(tenantId(row), userId(row))
  }

  override def tableName: Tenant.Id = "core_user_tenants"

  def store(entity: TenantUser): Future[ResultSet] = {
    insert
      .value(_.tenantId, entity.tenantId)
      .value(_.userId, entity.userId)
      .future()
  }

  def store(tenantId: Tenant.Id, userId: User.Id): Future[ResultSet] = {
    insert
      .value(_.tenantId, tenantId)
      .value(_.userId, userId)
      .future()
  }

  def getById(tenantId: Tenant.Id, userId: User.Id): Future[Option[TenantUser]] = {
    select.where(_.tenantId eqs tenantId).and(_.userId eqs userId).one()
  }

  def getByUserId(userId: User.Id) = {
    select.where(_.userId eqs userId).fetch()
  }

  def deleteById(tenantId: Tenant.Id, userId: User.Id): Future[ResultSet] = {
    delete.where(_.tenantId eqs tenantId).and(_.userId eqs userId).future()
  }

  def isExist(tenantId: Tenant.Id, userId: User.Id): Future[Boolean] = {
    getById(tenantId, userId).map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
