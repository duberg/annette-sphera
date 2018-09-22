package annette.core.utils

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import scala.util.Random
import Generator._

trait Generator {
  def generateId: String = s"i$generateInt"
  def generateIdOpt = Option(s"i$generateInt")
  def generateInt: Int = id.incrementAndGet()
  def generateIntOpt = Option(id.incrementAndGet())
  def generateUUID: UUID = UUID.randomUUID()
  def generateUUIDOpt = Option(UUID.randomUUID())
  def generateTestName = s"gen-test${testId.incrementAndGet()}"
  def generateString(x: Int = 10): String =
    Random.alphanumeric
      .take(x)
      .mkString
      .toLowerCase
  def generateFileName(extension: String): String = s"${generateString(8).toLowerCase}.$extension"
  def generateInt(start: Int, end: Int): Int = {
    val rnd = new Random
    start + rnd.nextInt(end - start + 1)
  }
  def generatePin: Int = generateInt(1000, 9999)
  def generatePinString: String = generatePin.toString
  def hide(x: String): String = x.map(_ => "*").mkString
  def generateEmail = s"${generateString(12)}@${generateString(5)}.ru"
  def generateEmailOpt = Option(generateEmail)
  def generatePassword: String = generateString(8)
  def generatePhone: String = s"+791666${generateInt(10000, 99999)}"
}

object Generator {
  private val id = new AtomicInteger(0)
  private val testId = new AtomicInteger(0)
}
