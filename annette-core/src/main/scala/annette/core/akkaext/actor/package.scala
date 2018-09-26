package annette.core.akkaext

package object actor {
  type RawId = String
  type RawPath = String

  implicit def toPath(x: ActorId): Path = Path(x)
  implicit def toDataPath(x: String): DataPath = DataPath(x)
  implicit def toDataPath(x: List[String]): DataPath = DataPath(x)
  implicit def toActorId(x: String): ActorId = ActorId(x)
  implicit def toActorIdOpt(x: String): Option[ActorId] = Some(ActorId(x))
  implicit def toActorIdOpt(x: Option[String]): Option[ActorId] = x map ActorId.apply
}
