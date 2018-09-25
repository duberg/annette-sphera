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
