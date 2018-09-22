/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.language.dao.tables

import annette.core.domain.language.model.Language
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

/**
 * Created by valery on 17.12.16.
 */

protected[dao] abstract class LanguageTable extends Table[LanguageTable, Language] with RootConnector {

  object name extends StringColumn
  object id extends StringColumn with PartitionKey

  override def fromRow(row: Row): Language = {
    Language(name(row), id(row))
  }

  override def tableName: String = "core_languages"

  def store(entity: Language): Future[ResultSet] = {
    insert
      .value(_.id, entity.id)
      .value(_.name, entity.name)
      .future()
  }

  def getById(id: Language.Id): Future[Option[Language]] = {
    select.where(_.id eqs id).one()
  }

  def deleteById(id: Language.Id): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }

  def isExist(id: Language.Id): Future[Boolean] = {
    select(_.id).where(_.id eqs id).one().map(_.isDefined)
  }

  def selectAll = {
    select.fetch
  }
}
