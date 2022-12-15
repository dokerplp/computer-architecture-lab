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

  test("Translator test 1") {

    val code: String =
      """
        |var x = 1 + 2 + 3 + 4 + 5
        |print(x)
        |var y = x + x + 34
        |print(y)
        |var z = y - x + 1 + y
        |z += z
        |print(z)
        |x = z - x + y
        |y = x - y + z
        |z = y - z + x
        |print(x)
        |print(y)
        |print(z)
        |""".stripMargin

    Files.write(Paths.get(js), code.getBytes(StandardCharsets.UTF_8))

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "15 64 228 277 441 490")
  }

  test("Translator test 2") {
    val code: String =
      """
        |var x = "aboba"
        |var y = x
        |print(y)
        |""".stripMargin

    Files.write(Paths.get(js), code.getBytes(StandardCharsets.UTF_8))

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log, str = true)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "aboba")
  }

}
