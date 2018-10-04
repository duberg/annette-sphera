
package annette.core.domain.language.dao

import akka.util.Timeout
import annette.core.domain.language.model.{ Language, LanguageUpdate }
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait ILanguageDao {
  implicit val serviceTimeout: Timeout = 30.seconds
  def create(application: Language)(implicit ec: ExecutionContext): Future[Unit]
  def update(application: LanguageUpdate)(implicit ec: ExecutionContext): Future[Unit]
  def getById(id: Language.Id)(implicit ec: ExecutionContext): Future[Option[Language]]
  def selectAll(implicit ec: ExecutionContext): Future[scala.List[Language]]
  def delete(id: Language.Id)(implicit ec: ExecutionContext): Future[Unit]
}
