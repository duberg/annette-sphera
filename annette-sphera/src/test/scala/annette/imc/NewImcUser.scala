package annette.imc

import akka.actor.ActorRef
import akka.pattern.ask
import annette.core.test.PersistenceSpec
import annette.imc.user.ImcUserActor
import annette.imc.user.model.ImcUser

import scala.concurrent.Future

trait NewImcUser extends NewUserDao { _: PersistenceSpec =>
  val imcUserA = ImcUser(
    userIdA,
    Option("""Генеральный директор ООО "Медицинские технологиии", профессор, д.м.н., инстутута им. Достоевского, заслуженный врач РФ"""),
    None,
    None,
    None)

  val imcUserB = ImcUser(
    userIdA,
    Option("Заведующая кафедрой общественного здоровья и здравоохранения инстутута им. Достоевского, профессор, д.м.н., заслуженный врач РФ"),
    None,
    None,
    None)

  val imcUserC = ImcUser(
    userIdA,
    Option("Доцент кафедры общественного здоровья и здравоохранения инстутута им. Достоевского"),
    None,
    None,
    None)

  val imcUserD = ImcUser(
    userIdA,
    Option("Доцент кафедры общественного здоровья и здравоохранения инстутута им. Достоевского"),
    None,
    None,
    None)

  val imcUsers = Set(
    imcUserA,
    imcUserB,
    imcUserC,
    imcUserD)

  def newImcUser(id: String = generateId): Future[ActorRef] =
    Future(system.actorOf(ImcUserActor.props(id), id))

  def createImcUser(a: ActorRef, x: ImcUser): Future[Any] =
    ask(a, ImcUserActor.CreateCmd(x))

  def createImcUsers(a: ActorRef, x: Set[ImcUser]): Future[Set[Any]] =
    Future.sequence(x.map(u => createImcUser(a, u)))

  def newFilledImcUser: Future[ActorRef] =
    for {
      a <- newImcUser()
      _ <- createImcUsers(a, imcUsers)
    } yield a
}