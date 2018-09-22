/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.authorization.dao
import akka.Done
import annette.core.domain.authorization.model.Permission

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by valery on 01.03.17.
 */
trait PermissionDao {

  def create(permission: Permission)(implicit ec: ExecutionContext): Future[Permission]

  def update(permission: Permission)(implicit ec: ExecutionContext): Future[Permission]

  def delete(id: String)(implicit ec: ExecutionContext): Future[Done]

  def getById(id: String)(implicit ec: ExecutionContext): Future[Option[Permission]]

  def selectAll(implicit ec: ExecutionContext): Future[List[Permission]]
}
