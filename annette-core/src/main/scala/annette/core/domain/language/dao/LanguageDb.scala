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
