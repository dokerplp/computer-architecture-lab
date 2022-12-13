package translator

import org.scalatest.funsuite.AnyFunSuite

class TranslatorTest extends AnyFunSuite {

  val translator = new Translator

  test("translation tier 1 test") {
    translator.translate("./jaba/fibonacci.js", "./jaba/fibonacci.as")
    println()
  }
}
