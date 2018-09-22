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

import javax.inject._

import annette.core.domain.DB
import annette.core.domain.application.dao.tables.ApplicationTable
import com.outworkers.phantom.dsl._

/**
 * Created by valery on 17.12.16.
 */

class ApplicationDb @Inject() (val db: DB) extends Database[ApplicationDb](db.keySpaceDef) {

  object applications extends ApplicationTable with db.keySpaceDef.Connector

}
