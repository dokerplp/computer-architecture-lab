package translator

import exception.HLTException
import machine.Memory
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source

class ISATest extends AnyWordSpec {

  val as = "./src/test/resources/test.as"
  val in = "./src/test/resources/test.in"
  val out = "./src/test/resources/test.out"
  val log = "./src/test/resources/test.log"

  def isa(code: String, str: Boolean = false): ISA = {
    Files.write(Paths.get(as), code.getBytes(StandardCharsets.UTF_8))
    val isa = new ISA
    isa.translate(as = as, in = in, out = out, log = log, str = str)
    isa
  }

  "ISA" should {
    "throw IllegalArgumentException on toBinary" in {
      val code =
        """
          |x: 5
          |start: ADD $x
          |HLT
          |""".stripMargin

      isa(code)

      val code2 =
        """
          |x: 5
          |start: ADC $x
          |HLT
          |""".stripMargin

      the[IllegalArgumentException] thrownBy isa(code2)
    }

    "set start address" in {
      val code =
        """
          |x: 5
          |start: ADD $x
          |HLT
          |""".stripMargin

      val isa1 = isa(code)
      isa1.user.processor.memory.reg(Memory.AddrRegister.IP) should equal (3)

      val code2 =
        """
          |ORG 6EE
          |x: 5
          |start: ADD $x
          |HLT
          |""".stripMargin

      val isa2 = isa(code2)
      isa2.user.processor.memory.reg(Memory.AddrRegister.IP) should equal (0x6F1)
    }
  }

}
