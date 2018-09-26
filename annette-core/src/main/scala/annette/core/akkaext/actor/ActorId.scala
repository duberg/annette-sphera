package annette.core.akkaext.actor

/**
 * ActorId is full path to actor.
 */
case class ActorId(fragments: List[String]) extends PathLike[ActorId] {
  def @/(dataPath: String) = Path(this, DataPath(dataPath))
  def @/(dataPath: DataPath) = Path(this, dataPath)
  def withFragments(fragments: List[String]) = copy(fragments = fragments)
}

object ActorId extends PathExtractor {
  def apply(path: String): ActorId = ActorId(fragments(path))
}