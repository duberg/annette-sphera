/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.dao.tables

import annette.core.domain.authorization.model.Permission
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class PermissionTable extends Table[PermissionTable, Permission] with RootConnector {

  object id extends StringColumn with PartitionKey
  object description extends StringColumn

  override def fromRow(row: Row): Permission = {
    Permission(id(row), description(row))
  }

  override def tableName: String = "core_permissions"

  def store(entity: Permission): Future[ResultSet] = {
    insert
      .value(_.id, entity.id)
      .value(_.description, entity.description)
      .future()
  }

  def getById(id: String): Future[Option[Permission]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: String): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def isExist(id: String): Future[Boolean] = {
    select(_.id).where(_.id eqs id).one().map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
