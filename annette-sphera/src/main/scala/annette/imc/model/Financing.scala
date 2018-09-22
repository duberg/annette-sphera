package annette.imc.model

case class Financing(nameMessage: String)
object Financing {
  private val root = ""
  val LOANS: String = root + "loans"
  val OWNFUNDS: String = root + "ownfunds"
  val LOOKINGFORINVESTORS: String = root + "lookingforinvestors"
  def make(shortName: String): Financing = Financing(root + shortName)
}
