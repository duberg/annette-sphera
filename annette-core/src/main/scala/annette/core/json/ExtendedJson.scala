package annette.core.json

import io.circe.{ Json, JsonObject }

trait ExtendedJson {
  implicit class JsonOpt(json: Json) {
    //def toObject: JsonObject = json.asObject.getOrElse(throw JsonIsNotJsonObjectException(json))
    //def toMap: Map[String, Json] = json.toObject.toMap
  }
}
