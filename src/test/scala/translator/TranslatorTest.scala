package translator

import org.scalatest.funsuite.AnyFunSuite

class TranslatorTest extends AnyFunSuite {

  val translator = new Translator2

  test("translation tier 1 test") {
    translator.translate("./jaba/fibonacci.js", "")
    println()
  }
}
