package annette.core.domain.tenancy.dao

import akka.util.Timeout
import annette.core.domain.tenancy.model.{ User, UserUpdate }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait IUserDao {
  implicit val serviceTimeout: Timeout = 30.seconds
  def create(user: User, password: String)(implicit ec: ExecutionContext): Future[Unit]
  def update(user: UserUpdate)(implicit ec: ExecutionContext): Future[Unit]
  def setPassword(userId: User.Id, password: String)(implicit ec: ExecutionContext): Future[Boolean]
  def delete(id: User.Id)(implicit ec: ExecutionContext): Future[Boolean]
  def getById(id: User.Id)(implicit ec: ExecutionContext): Future[Option[User]]
  def selectAll(implicit ec: ExecutionContext): Future[List[User]]
  def getByLoginAndPassword(login: String, password: String)(implicit ec: ExecutionContext): Future[Option[User]]
}
