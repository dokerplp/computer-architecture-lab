package translator

import org.scalatest.funsuite.AnyFunSuite

class TranslatorTest extends AnyFunSuite {

  val translator = new Translator
  val isa = new Isa

  test("translation tier 1 test") {
    translator.translate("./jaba/fibonacci.js", "./jaba/fibonacci.as")
    println()
  }

  test("isa test") {
    isa.translate("./jaba/fibonacci.as", "")
  }


}
