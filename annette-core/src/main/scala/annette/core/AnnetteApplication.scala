package annette.core

object AnnetteApplication extends App {
  val annetteServer = new AnnetteServer()
  annetteServer.run()
}
