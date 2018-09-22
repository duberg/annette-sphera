/***************************************************************************************
* Copyright (c) 2014-2017 by Valery Lobachev
* Redistribution and use in source and binary forms, with or without
* modification, are NOT permitted without written permission from Valery Lobachev.
*
* Copyright (c) 2014-2017 Валерий Лобачев
* Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
* запрещено без письменного разрешения правообладателя.
****************************************************************************************/

package annette.core.domain.language.dao

import javax.inject._

import annette.core.domain.DB
import annette.core.domain.language.dao.tables.LanguageTable
import com.outworkers.phantom.dsl.Database

/**
 * Created by valery on 17.12.16.
 */
@Singleton
class LanguageDb @Inject() (val db: DB) extends Database[LanguageDb](db.keySpaceDef) {

  object languages extends LanguageTable with db.keySpaceDef.Connector

}
