package machine

import exception.HLTException
import machine.AddressedCommand.Type.ABSOLUTE
import machine.AddressedCommand.*
import machine.UnaddressedCommand.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.equal
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.should.Matchers.the
import org.scalatest.wordspec.AnyWordSpec

class UserTest extends AnyWordSpec {

  val user = new User

  "Program" should {
    "load given values to memory" in {
      val program = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

      user.load(program, 0)
      val data = (0 to 10).map(user.processor.memory.mem(_))
      data should equal (program)
    }
    
    "print fibonacci numbers" in {
      //(value, address)
      val t = (0, 0)
      val x1 = (1, 1)
      val x2 = (1, 2)
      val n = (5, 3)
      val loop = (-1, 8)
      val end = (-1, 22)

      val program = List(
        t._1,
        x1._1,
        x2._1,
        n._1,
        LD(x1._2, ABSOLUTE),
        OUT.bin,
        LD(x2._2, ABSOLUTE),
        OUT.bin,
        LOOP(n._2, ABSOLUTE),
        JUMP(end._2, ABSOLUTE),
        LD(x1._2, ABSOLUTE),
        ST(t._2, ABSOLUTE),
        LD(x2._2, ABSOLUTE),
        ST(x1._2, ABSOLUTE),
        LD(x2._2, ABSOLUTE),
        ADD(t._2, ABSOLUTE),
        ST(x2._2, ABSOLUTE),
        OUT.bin,
        LD(n._2, ABSOLUTE),
        DEC.bin,
        ST(n._2, ABSOLUTE),
        JUMP(loop._2, ABSOLUTE),
        HLT.bin
      )

      user.load(program, 0)

      the[HLTException] thrownBy user.run(4)
      user.processor.memory.mem(x2._2) should equal (13)
      user.device.output should equal (List(1, 1, 2, 3, 5, 8, 13))
    }
    
  }

    
}
