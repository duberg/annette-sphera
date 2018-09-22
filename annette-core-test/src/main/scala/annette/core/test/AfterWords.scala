package annette.core.test

import org.scalatest.AsyncWordSpecLike

trait AfterWords { self: AsyncWordSpecLike =>
  def provide: AfterWord = afterWord("provide")
  def receive: AfterWord = afterWord("receive")
  def response: AfterWord = afterWord("response")
  def call: AfterWord = afterWord("call")
  def responseWithCode: AfterWord = afterWord("response with http code")
}
