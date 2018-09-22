/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.application.dao

import akka.util.Timeout
import annette.core.domain.application.model.{ Application, ApplicationUpdate }
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait IApplicationDao {
  implicit val serviceTimeout: Timeout = 30.seconds
  def create(application: Application)(implicit ec: ExecutionContext): Future[Unit]
  def update(application: ApplicationUpdate)(implicit ec: ExecutionContext): Future[Unit]
  def getById(id: Application.Id)(implicit ec: ExecutionContext): Future[Option[Application]]
  def selectAll(implicit ec: ExecutionContext): Future[scala.List[Application]]
  def delete(id: Application.Id)(implicit ec: ExecutionContext): Future[Unit]
}
