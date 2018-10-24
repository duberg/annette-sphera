package annette.app.exchange

import akka.actor.ActorRef

import scala.concurrent.{ ExecutionContext, Future }
import Exchange._
import akka.util.Timeout
import akka.pattern.ask

class ExchangeService(actor: ActorRef)(implicit c: ExecutionContext, t: Timeout) {
  def getRates: Future[Rates] =
    ask(actor, GetRates)
      .mapTo[Rates]

  def convert(x: Convert): Future[ConvertRes] =
    ask(actor, x)
      .mapTo[ConvertRes]
}
