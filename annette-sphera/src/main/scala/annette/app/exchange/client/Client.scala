package annette.app.exchange.client

import scala.concurrent.Future

object Client {
  case class Rates(base: String, rates: Map[String, Double])
}

trait ClientLike {
  def rates(): Future[Client.Rates]
}
