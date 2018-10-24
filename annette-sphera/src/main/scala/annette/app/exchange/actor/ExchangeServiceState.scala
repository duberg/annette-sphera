package annette.app.exchange.actor

import annette.core.akkaext.actor.CqrsState
import annette.app.exchange.Exchange.RatesUpdatedEvt

case class ExchangeServiceState(base: String, rates: Map[String, Double]) extends CqrsState {
  def update = {
    case RatesUpdatedEvt(x, y) => copy(base = x, rates = y)
  }
}

object ExchangeServiceState {
  def empty = ExchangeServiceState(base = "EUR", rates = Map.empty)
}
