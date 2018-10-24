package annette.app.exchange

import annette.app.exchange.client.Client.Rates
import annette.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsQuery, CqrsResponse }
import annette.core.domain.application.{ Application, UpdateApplication }

object Exchange {
  trait Command extends CqrsCommand
  trait Query extends CqrsQuery
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case object UpdateCurrenciesCmd extends Command

  object GetRates extends Query

  case class Convert(data: List[ConvertEntry]) extends Query
  case class ConvertEntry(currencyFrom: String, currencyTo: String, valueFrom: Double)

  case class RatesUpdatedEvt(base: String, rates: Map[String, Double]) extends Event

  case class Rates(base: String, rates: Map[String, Double]) extends Response
  case class ConvertRes(data: List[ConvertResEntry], errorCode: Int, errorMessage: String)
  case class ConvertResEntry(currencyFrom: String, currencyTo: String, valueFrom: Double, valueTo: Double)
}
