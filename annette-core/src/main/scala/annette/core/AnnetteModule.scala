package annette.core

import scala.concurrent.Future

trait AnnetteModule {
  def init(): Future[Unit]
  def name: String
  def buildInfo: String
  var coreModule: CoreModule = _
}
