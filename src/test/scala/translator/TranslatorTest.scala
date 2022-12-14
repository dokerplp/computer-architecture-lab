package translator

import org.scalatest.funsuite.AnyFunSuite

class TranslatorTest extends AnyFunSuite {

  val translator = new Translator
  val isa = new Isa

  //  test("fibonacci translator") {
  //    translator.translate("./jaba/fibonacci.js", "./jaba/fibonacci.as")
  //    println()
  //  }
  //
  //  test("fibonacci test") {
  //    isa.translate("./jaba/fibonacci.as", "")
  //  }

  test("hello world translator") {
    translator.translate("./lang/helloWorld.js", "./lang/helloWorld.as")
    println()
  }

  test("hello world test") {
    isa.translate("./lang/helloWorld.as", "")
  }


}
