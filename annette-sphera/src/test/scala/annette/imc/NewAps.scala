package annette.imc

import akka.actor.ActorRef
import akka.pattern.ask
import annette.core.domain.tenancy.model.User
import annette.core.test.PersistenceSpec
import annette.imc.model.ApFile.FileType
import annette.imc.model.{ Ap, ApFile }

import scala.concurrent.Future

trait NewAps extends NewUserDao { _: PersistenceSpec =>
  val apFileA = ApFile(
    id = generateUUID,
    name = generateId,
    lang = "RU",
    fileType = FileType(FileType.ApplicationFile),
    generateFileName("txt"))
  val apFileB = ApFile(
    id = generateUUID,
    name = generateId,
    lang = "RU",
    fileType = FileType(FileType.ApplicationFile),
    generateFileName("txt"))
  val apFiles = Set(apFileA, apFileB)

  def newAp(a: ActorRef, userId: User.Id): Future[Ap.Id] =
    ask(a, ApsActor.CreateCmd(userId))
      .mapTo[ApsActor.Created]
      .map(_.id)

  def addExpert(a: ActorRef, apId: Ap.Id, userId: User.Id): Future[Any] =
    ask(a, ApsActor.AddExpertCmd(apId, userId))

  def addExperts(a: ActorRef, apId: Ap.Id, users: Set[User.Id]): Future[Set[Any]] =
    Future.sequence(users.map(addExpert(a, apId, _)))

  //def addBulletin(a: ActorRef, apId: Ap.Id, users: Set[User.Id]) =
  //ask(a, ApsActor.UpdateBulletinCmd())

  def addFile(a: ActorRef, apId: Ap.Id, apFile: ApFile): Future[Any] =
    ask(a, ApsActor.AddFileCmd(apId, apFile))

  def addFiles(a: ActorRef, apId: Ap.Id, apFiles: Set[ApFile]): Future[Set[Any]] =
    Future.sequence(apFiles.map(addFile(a, apId, _)))

  def newAps(id: String = generateId, state: ApsState = ApsState(storage = ApsStorage())): Future[ActorRef] = Future {
    system.actorOf(ApsActor.props(id, state), id)
  }

  def newFilledAps(userId: User.Id): Future[(Ap.Id, ActorRef)] =
    for {
      a <- newAps()
      apId <- newAp(a, userId)
      _ <- addExperts(a, apId, userIds)
      _ <- addFiles(a, apId, apFiles)
    } yield (apId, a)
}