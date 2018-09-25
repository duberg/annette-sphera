package annette.core

import akka.http.scaladsl.server.Route

trait AnnetteHttpModule extends AnnetteModule {
  def routes: Route
}
