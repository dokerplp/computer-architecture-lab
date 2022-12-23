package translator

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.equal
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.should.Matchers.the
import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source

class TranslatorTest extends AnyWordSpec {

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

  "Translator" should {
    "correctly work with mathematical expressions" in {
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
          |x++
          |print(x)
          |y--
          |print(y)
          |""".stripMargin

      val res = compileAndRun(code)
      res should equal("15 64 228 277 441 490 278 440")
    }

    "correctly calculate fibonacci numbers" in {
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
      res should equal("13")
    }

    "print \"hello world!\"" in {
      val code =
        """
          |string hw = "hello world!"
          |print(hw)
          |""".stripMargin

      val res = compileAndRun(code, true)
      res should equal("hello world!")
    }
  }
}
