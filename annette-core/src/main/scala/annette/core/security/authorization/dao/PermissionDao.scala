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
