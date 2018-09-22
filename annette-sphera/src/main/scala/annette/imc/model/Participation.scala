package annette.imc.model

case class Participation(nameMessage: String)

object Participation {
  private val root = ""
  val OPERATORONLY: String = root + "operatoronly"
  val OPERATORANDINVESTOR: String = root + "operatorandinvestor"
  def make(shortName: String): Participation = Participation(root + shortName)
}
