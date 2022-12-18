package translator

import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.io.Source

class TranslatorTest extends AnyFunSuite {

  val js = "./src/test/resources/test.js"
  val as = "./src/test/resources/test.as"
  val in = "./src/test/resources/test.in"
  val out = "./src/test/resources/test.out"
  val log = "./src/test/resources/test.log"

  def compileAndRun(code: String, str: Boolean = false): String = {
    Files.write(Paths.get(js), code.getBytes(StandardCharsets.UTF_8))

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log, str = str)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString
    src.close()

    res
  }

  test("Translator test") {
    val code: String =
      """
        |int x = 1 + 2 + 3 + 4 + 5
        |print(x)
        |int y = x + x + 34
        |print(y)
        |int z = y - x + 1 + y
        |z = z + z
        |print(z)
        |x = z - x + y
        |y = x - y + z
        |z = y - z + x
        |print(x)
        |print(y)
        |print(z)
        |""".stripMargin

    val res = compileAndRun(code)
    assert(res == "15 64 228 277 441 490")
  }

   test("fibonacci numbers test") {
     val code =
       """
         |int x = 1
         |int y = 1
         |int n = 5
         |int t = 1
         |while (n)
         |    t = x
         |    x = y
         |    y = y + t
         |    n--
         |end while
         |print(y)
         |""".stripMargin

     val res = compileAndRun(code)
     assert(res == "13")
   }

   test("hello world test") {
     val code =
       """
         |string hw = "hello world!"
         |print(hw)
         |""".stripMargin

     val res = compileAndRun(code, true)
     assert(res == "hello world!")
   }

}
