package annette.app.exchange.actor

import akka.actor.Props
import akka.util.Timeout
import annette.app.exchange._
import annette.core.akkaext.actor.CqrsActor
import annette.app.exchange.Exchange._
import annette.app.exchange.client.{ Client, ClientLike }

import scala.concurrent.{ ExecutionContext, Future }

class ExchangeServiceActor(val client: ClientLike, val initState: ExchangeServiceState)(implicit c: ExecutionContext, t: Timeout) extends CqrsActor[ExchangeServiceState] {
  def getRates(state: ExchangeServiceState) = {
    if (state.rates.isEmpty) after(client.rates()) { (response, state) =>
      changeState(state.updated(RatesUpdatedEvt(response.base, response.rates)))
      sender() ! Rates(base = response.base, rates = response.rates)
    }
    else sender() ! Rates(base = state.base, rates = state.rates)
  }

  def convert(state: ExchangeServiceState, x: List[ConvertEntry]) = {
    def clientConvert(x: ConvertEntry) = Future {
      ConvertResEntry(
        currencyFrom = x.currencyFrom,
        currencyTo = x.currencyTo,
        valueFrom = x.valueFrom,
        valueTo = (state.rates.get(x.currencyFrom) flatMap { from =>
          state.rates.get(x.currencyTo) map { to =>
            x.valueFrom * to / from
          }
        }).getOrElse(0))
    }

    Future.sequence(x.map(clientConvert)).map(x => ConvertRes(
      data = x,
      errorCode = 0,
      errorMessage = "No errors")) pipeTo sender()
  }

  def behavior(state: ExchangeServiceState) = {
    case GetRates => getRates(state)
    case Convert(x) => convert(state, x)
  }
}

object ExchangeServiceActor {
  def props(client: ClientLike, state: ExchangeServiceState = ExchangeServiceState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ExchangeServiceActor(
      client = client,
      initState = state))
  }
}
