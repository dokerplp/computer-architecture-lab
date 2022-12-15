package machine

import org.scalatest.funsuite.AnyFunSuite
import machine.Memory.AddrRegister._
import machine.Memory.DataRegister._
import exception.HLTException
import machine.ControlUnit
import org.mockito.Mockito.*
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar

class ControlUnitTest extends AnyFunSuite
  with BeforeAndAfter
  with BeforeAndAfterEach
  with MockitoSugar
{

  var tg = new TactGenerator
  var memory = new Memory
  val device = new Device
  var unit: ControlUnit = new ControlUnit(tg, memory, device)

  override def beforeEach(): Unit = {
    tg = new TactGenerator
    memory = new Memory
    unit = new ControlUnit(tg = tg, memory = memory, device = device)
  }

  test("writeIP test") {
    device.IO = 12
    unit.writeIP()
    assert(memory.reg(IP) == 12)
  }

  test("input test") {
    device.IO = 10
    unit.input()
    assert(memory.mem(0) == 10)

    device.IO = 11
    unit.input()
    assert(memory.mem(1) == 11)
  }


  test("absolute operand fetch test") {
    memory.reg(DR) = 0x2113
    memory.mem(0x113) = 42
    unit.operandFetch()

    assert(memory.reg(DR) == 42)
  }

  test("direct operand fetch test") {
    memory.reg(DR) = 0x2813
    unit.operandFetch()

    assert(memory.reg(DR) == 0x13)
  }

  test("relative operand fetch test") {
    memory.reg(DR) = 0x2c13
    memory.mem(0x13) = 0x42
    memory.mem(0x42) = 0x567
    unit.operandFetch()

    assert(memory.reg(DR) == 0x567)
  }

  test("command fetch test") {
    memory.mem(0) = 0x1000
    memory.mem(1) = 0xF100
    val spyUnit = spy(unit)

    doThrow(classOf[IllegalArgumentException]).when(spyUnit).add()
    doThrow(classOf[IllegalStateException]).when(spyUnit).hlt()

    assertThrows[IllegalArgumentException] {
      spyUnit.commandFetch()
    }

    assertThrows[IllegalStateException] {
      spyUnit.commandFetch()
    }

  }

  test("add test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(DR) = 12
    memory.reg(AC) = 13

    spyUnit.add()
    assert(memory.reg(AC) == 25)
  }

  test("sub test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(DR) = 12
    memory.reg(AC) = 13

    spyUnit.sub()
    assert(memory.reg(AC) == 1)
  }

  test("loop test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(DR) = 1
    spyUnit.loop()

    assert(memory.reg(IP) == 1)

    memory.reg(DR) = 0
    spyUnit.loop()

    assert(memory.reg(IP) == 1)
  }

  test("ld test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(DR) = 42

    spyUnit.ld()

    assert(memory.reg(AC) == 42)
  }

  test("st test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 42

    spyUnit.st()

    assert(memory.mem(0) == 42)
  }

  test("jump test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(DR) = 0x6128

    spyUnit.jump()

    assert(memory.reg(IP) == 0x128)
  }

  test("jz test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 1
    memory.reg(DR) = 0x6128

    spyUnit.jz()

    assert(memory.reg(IP) == 0)

    memory.reg(AC) = 0
    memory.reg(DR) = 0x7043

    spyUnit.jz()

    assert(memory.reg(IP) == 0x43)
  }

  test("hlt test") {
    assertThrows[HLTException] {
      unit.hlt()
    }
  }

  test("cla test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 42

    spyUnit.cla()

    assert(memory.reg(AC) == 0)
  }

  test("inc test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 42

    spyUnit.inc()

    assert(memory.reg(AC) == 43)
  }

  test("dec test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 42

    spyUnit.dec()

    assert(memory.reg(AC) == 41)
  }

  test("null test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    val memBefore = new Memory

    spyUnit._null()

    assert(memBefore.addrRegisters == memory.addrRegisters)
    assert(memBefore.dataRegisters == memory.dataRegisters)
  }

  test("zero test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = 12
    memory.reg(DR) = 6

    spyUnit.sub()
    assert(!memory.zero)

    spyUnit.sub()
    assert(memory.zero)
  }

  test("fix value test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(AC) = Memory.MAX_WORD
    spyUnit.inc()
    assert(memory.reg(AC) == Memory.MIN_WORD)

    memory.reg(AC) = Memory.MIN_WORD
    spyUnit.dec()
    assert(memory.reg(AC) == Memory.MAX_WORD)
  }

  test("fix address test") {
    val spyUnit = spy(unit)
    doNothing().when(spyUnit).commandFetch()

    memory.reg(IP) = Memory.MAX_ADDR
    spyUnit.input()

    assert(memory.reg(IP) == 0)
  }

}