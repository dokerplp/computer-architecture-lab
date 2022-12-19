package machine

import exception.HLTException
import machine.AddressedCommand.Type.ABSOLUTE
import machine.AddressedCommand.*
import machine.ControlUnit
import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import machine.UnaddressedCommand.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.convertToStringShouldWrapperForVerb
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class ControlUnitTest extends AnyWordSpec
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterEach
  with MockitoSugar {

  val device = new Device
  var tg = new TactGenerator
  var memory = new Memory
  var unit: ControlUnit = new ControlUnit(tg, memory, device)

  override def beforeEach(): Unit = {
    tg = new TactGenerator
    memory = new Memory
    unit = new ControlUnit(tg = tg, memory = memory, device = device)
  }

  "The Control Unit" should {
    "change IP register on writeIP" in {
      device.IO = 12
      unit.writeIP()

      memory.reg(IP) should equal (12)
    }

    "add values to memory on input" in {
      device.IO = 10
      unit.input()

      memory.mem(0) should equal (10)

      device.IO = 11
      unit.input()

      memory.mem(1) should equal (11)
    }

    "put the value located at the address into the DR on operandFetch" in {
      memory.reg(DR) = 0x2113
      memory.mem(0x113) = 42
      unit.operandFetch()

      memory.reg(DR) should equal (42)
    }

    "put the value into the DR after on operandFetch" in {
      memory.reg(DR) = 0x2813
      unit.operandFetch()

      memory.reg(DR) should equal (0x13)
    }

    "put the value located at the address specified in the passed address into the DR on operandFetch" in {
      memory.reg(DR) = 0x2c13
      memory.mem(0x13) = 0x42
      memory.mem(0x42) = 0x567
      unit.operandFetch()

      memory.reg(DR) should equal (0x567)
    }

    "call right command on commandFetch" in {
      memory.mem(0) = ADD(0, ABSOLUTE)
      memory.mem(1) = SUB(0, ABSOLUTE)
      memory.mem(2) = LOOP(0, ABSOLUTE)
      memory.mem(3) = LD(0, ABSOLUTE)
      memory.mem(4) = ST(0, ABSOLUTE)
      memory.mem(5) = JUMP(0, ABSOLUTE)
      memory.mem(6) = JZ(0, ABSOLUTE)
      memory.mem(7) = NULL.bin
      memory.mem(8) = HLT.bin
      memory.mem(9) = CLA.bin
      memory.mem(10) = INC.bin
      memory.mem(11) = DEC.bin
      memory.mem(12) = IN.bin
      memory.mem(13) = OUT.bin

      val spyUnit = spy(unit)

      doThrow(new IllegalArgumentException("add")).when(spyUnit).add()
      doThrow(new IllegalArgumentException("sub")).when(spyUnit).sub()
      doThrow(new IllegalArgumentException("loop")).when(spyUnit).loop()
      doThrow(new IllegalArgumentException("ld")).when(spyUnit).ld()
      doThrow(new IllegalArgumentException("st")).when(spyUnit).st()
      doThrow(new IllegalArgumentException("jump")).when(spyUnit).jump()
      doThrow(new IllegalArgumentException("jz")).when(spyUnit).jz()
      doThrow(new IllegalArgumentException("null")).when(spyUnit)._null()
      doThrow(new IllegalArgumentException("hlt")).when(spyUnit).hlt()
      doThrow(new IllegalArgumentException("cla")).when(spyUnit).cla()
      doThrow(new IllegalArgumentException("inc")).when(spyUnit).inc()
      doThrow(new IllegalArgumentException("dec")).when(spyUnit).dec()
      doThrow(new IllegalArgumentException("in")).when(spyUnit).in()
      doThrow(new IllegalArgumentException("out")).when(spyUnit).out()


      var thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("add")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("sub")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("loop")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("ld")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("st")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("jump")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("jz")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("null")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("hlt")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("cla")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("inc")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("dec")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("in")

      thrown = the[IllegalArgumentException] thrownBy spyUnit.commandFetch()
      thrown.getMessage should equal ("out")
    }

    "add the passed value to the accumulator on add" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(DR) = 12
      memory.reg(AC) = 13

      spyUnit.add()
      memory.reg(AC) should equal (25)
    }

    "subtract the passed value from the accumulator on sub" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(DR) = 12
      memory.reg(AC) = 13

      spyUnit.sub()
      memory.reg(AC) should equal (1)
    }

    "increment IP if value is greater than 0 on loop" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(DR) = 1
      spyUnit.loop()

      memory.reg(IP) should equal (1)

      memory.reg(DR) = 0
      spyUnit.loop()

      memory.reg(IP) should equal (1)
    }

    "load value into accumulator on ld" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(DR) = 42

      spyUnit.ld()

      memory.reg(AC) should equal (42)
    }

    "save value from accumulator into memory on st" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 42

      spyUnit.st()

      memory.mem(0) should equal (42)
    }

    "change IP to given address on jump" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(DR) = 0x6128

      spyUnit.jump()

      memory.reg(IP) should equal (0x128)
    }

    "change IP to given address if zero flag is true on jz" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 1
      memory.reg(DR) = 0x6128

      spyUnit.jz()

      memory.reg(IP) should equal (0)

      memory.reg(AC) = 0
      memory.reg(DR) = 0x7043

      spyUnit.jz()

      memory.reg(IP) should equal (0x43)
    }

    "throw [HLTException] on hlt" in {
      the[HLTException] thrownBy unit.hlt()
    }

    "clear accumulator on cla" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 42

      spyUnit.cla()

      memory.reg(AC) should equal (0)
    }

    "increment accumulator on inc" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 42

      spyUnit.inc()

      memory.reg(AC) should equal (43)
    }

    "decrement accumulator on dec" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 42

      spyUnit.dec()

      memory.reg(AC) should equal (41)
    }

    "do nothing on null" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      val memBefore = new Memory

      spyUnit._null()

      memBefore.addrRegisters should equal (memory.addrRegisters)
      memBefore.dataRegisters should equal (memory.dataRegisters)
    }

    "correctly set zero flag" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = 12
      memory.reg(DR) = 6

      spyUnit.sub()

      memory.zero should equal (false)

      spyUnit.sub()
      memory.zero should equal (true)
    }

    "correctly fix data register" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(AC) = Memory.MAX_WORD
      spyUnit.inc()
      memory.reg(AC) should equal (Memory.MIN_WORD)

      memory.reg(AC) = Memory.MIN_WORD
      spyUnit.dec()
      memory.reg(AC) should equal (Memory.MAX_WORD)
    }

    "correctly fix address register" in {
      val spyUnit = spy(unit)
      doNothing().when(spyUnit).commandFetch()

      memory.reg(IP) = Memory.MAX_ADDR
      spyUnit.input()

      memory.reg(IP) should equal (0)

      memory.reg(IP) = -1

      memory.reg(IP) should equal (Memory.MAX_ADDR)
    }

  }
}