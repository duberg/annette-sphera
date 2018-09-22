/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.application.dao.tables

import annette.core.domain.application.model.Application
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class ApplicationTable extends Table[ApplicationTable, Application] with RootConnector {

  object id extends StringColumn with PartitionKey
  object name extends StringColumn
  object code extends StringColumn

  override def fromRow(row: Row): Application = {
    Application(name(row), code(row), id(row))
  }

  override def tableName: String = "core_applications"

  def store(entity: Application): Future[ResultSet] = {
    insert
      .value(_.id, entity.id)
      .value(_.name, entity.name)
      .value(_.code, entity.code)
      .future()
  }

  def getById(id: Application.Id): Future[Option[Application]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: Application.Id): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def isExist(id: Application.Id): Future[Boolean] = {
    select(_.id).where(_.id eqs id).one().map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
