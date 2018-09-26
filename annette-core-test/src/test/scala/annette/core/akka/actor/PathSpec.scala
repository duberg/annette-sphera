package annette.core.akka.actor

import annette.core.akkaext.actor.{ ActorId, DataPath, Path, SelectParentAsterisk }
import annette.core.utils.Generator
import org.scalatest.{ Matchers, WordSpec }

class PathSpec extends WordSpec with Matchers with Generator {
  "A Path" must {
    "be created" in {
      val path1 = generateActorId @/ generateUUIDStr / "age" >/ "../*"
      val path2 = generateActorId @/ generateUUIDStr / "age"
      val path3 = Path(List(generateActorIdStr + "@", generateUUIDStr, "age")).raw
      val path4 = Path(List(
        "8d05480d-c14a-4b67-a389-1786f5ac4234/bpm53/storage/c2933f8d-94d0-42fa-9725-709a1f44b7d0@",
        "secretary")).raw
      val path5 = Path(List(
        "8d05480d-c14a-4b67-a389-1786f5ac4234.bpm53.storage.c2933f8d-94d0-42fa-9725-709a1f44b7d0@",
        "secretary", "age")).raw
      val path6 = DataPath(generateUUIDStr) / "age" >/ SelectParentAsterisk
      val path7 = (ActorId("id52") / "6de4e1c7-0a28-4a98-ae75-db02ef76ea0b" / "lastname").raw
      val path8 = Path("id52@/6de4e1c7-0a28-4a98-ae75-db02ef76ea0b/firstname")
      val path9 = Path("id52").raw
      val path10 = Path(path9)
      val path11 = Path(List("id87", "secretary", "firstname")).raw
      println(path11)
      succeed
    }
  }
}