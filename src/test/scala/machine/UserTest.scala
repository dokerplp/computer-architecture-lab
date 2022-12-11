package machine

import exception.HLTException
import machine.ControlUnit.AddressCommands.*
import machine.ControlUnit.UnaddressedCommands.*
import org.scalatest.funsuite.AnyFunSuite

class UserTest extends AnyFunSuite {

  val user = new User

  test("load program test") {
    val program = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    user.load(program, 0)
    val data = (0 to 10).map(user.processor.memory.mem(_))
    assert(data == program)
  }

  test("fibonacci nums test") {

    //(value, address)
    val t = (0, 0)
    val x1 = (1, 1)
    val x2 = (1, 2)
    val n = (5, 3)
    val loop = (-1, 8)
    val end = (-1, 22)

    val program = List (
      t._1,
      x1._1,
      x2._1,
      n._1,
      LD(x1._2),
      OUT(),
      LD(x2._2),
      OUT(),
      LOOP(n._2),
      JUMP(end._2),
      LD(x1._2),
      ST(t._2),
      LD(x2._2),
      ST(x1._2),
      LD(x2._2),
      ADD(t._2),
      ST(x2._2),
      OUT(),
      LD(n._2),
      DEC(),
      ST(n._2),
      JUMP(loop._2),
      HLT()
    )

    user.load(program, 0)

    assertThrows[HLTException] {
      user.run(4)
    }

    assert(user.processor.memory.mem(x2._2) == 13)

    assert(user.processor.buffer == List(1, 1, 2, 3, 5, 8, 13))

    println(user.processor.log)
  }
}
