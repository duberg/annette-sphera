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
