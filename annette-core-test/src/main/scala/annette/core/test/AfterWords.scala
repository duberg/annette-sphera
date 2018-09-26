package annette.core.test

import org.scalatest.AsyncWordSpecLike

trait AfterWords { self: AsyncWordSpecLike =>
  def provide: AfterWord = afterWord("provide")
  def receive: AfterWord = afterWord("receive")
  def response: AfterWord = afterWord("result")
  def responseWithCode: AfterWord = afterWord("result with http code")
}
