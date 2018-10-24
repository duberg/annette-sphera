package annette.app.exchange.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

object FixerIoClient {

  def apply(config: Config)(implicit system: ActorSystem, m: Materializer, c: ExecutionContext): FixerIoClient = {
    new FixerIoClient(config)
  }

  case class Symbols(
    success: Boolean,
    symbols: Map[String, String])

  case class Query(
    from: String,
    to: String,
    amount: Double)

  case class Info(
    timestamp: Long,
    rate: Double)

  case class Convert(
    success: Boolean,
    query: Query,
    info: Info,
    result: Double)

  case class Latest(
    success: Boolean,
    base: String,
    rates: Map[String, Double])

}

class FixerIoClient(config: Config)(implicit system: ActorSystem, m: Materializer, c: ExecutionContext) extends ClientLike {
  val baseUrl: String = config.getString("base_url")
  val accessKey: String = config.getString("access_key")

  def request(endpoint: String, params: Map[String, String] = Map.empty): Future[HttpResponse] = {
    val uri = params.foldLeft(baseUrl + endpoint + s"?access_key=$accessKey") {
      case (acc, param) =>
        acc + s"&${param._1}=${param._2}"
    }
    Http().singleRequest(HttpRequest(uri = uri))
  }

  // "latest" endpoint - request the most recent exchange rate data
  def rates(): Future[Client.Rates] = {
    request("latest").flatMap(Unmarshal(_).to[Client.Rates]) map { response =>
      response.copy(rates = response.rates + (response.base -> 1))
    }
  }
}