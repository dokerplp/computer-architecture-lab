//package machine
//
//import org.scalatest.funsuite.AnyFunSuite
//import Memory.AddrRegister._
//import Memory.DataRegister._
//import exception.{HLTException, IllegalAddressException}
//import machine.ControlUnit
//import org.mockito.Mockito.*
//import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
//import org.scalatestplus.mockito.MockitoSugar
//
//class ControlUnitTest extends AnyFunSuite
//  with BeforeAndAfter
//  with BeforeAndAfterEach
//  with MockitoSugar
//{
//
//  var tg = new TactGenerator
//  var memory = new Memory
//  val device = new Device
//  var unit: ControlUnit = new ControlUnit(tg, memory, device)
//
//  override def beforeEach(): Unit = {
//    tg = new TactGenerator
//    memory = new Memory
//    unit = new ControlUnit(tg = tg, memory = memory, device = device)
//  }
//
//
//
//  test("writeIP test") {
//    device.IO = 12
//    unit.writeIP()
//    assert(memory.reg(IP) == 12)
//  }
//
//  test("input test") {
//    unit.input(10)
//    assert(memory.mem(0) == 10)
//
//    unit.input(11)
//    assert(memory.mem(1) == 11)
//
//    unit.input(12)
//    unit.input(13)
//    assert(memory.mem(3) == 13)
//  }
//
//  test("get addr from word test") {
//    val instr = 0x2113
//    assert(ControlUnit.m11(instr) == 0x113)
//  }
//
//  test("operand fetch test") {
//    memory.reg(DR) = 0x2113
//    val addr = 0x113
//    memory.mem(addr) = 42
//    unit.operandFetch()
//
//    assert(memory.reg(DR) == 42)
//  }
//
//  test("command fetch test") {
//    memory.mem(0) = 0x1000
//    memory.mem(1) = 0xF100
//    val spyUnit = spy(unit)
//
//    doThrow(classOf[IllegalArgumentException]).when(spyUnit).add()
//    doThrow(classOf[IllegalStateException]).when(spyUnit).hlt()
//
//    assertThrows[IllegalArgumentException] {
//      spyUnit.commandFetch()
//    }
//
//    assertThrows[IllegalStateException] {
//      spyUnit.commandFetch()
//    }
//
//  }
//
//  test("add test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 12
//    memory.reg(AC) = 13
//
//    spyUnit.add()
//    assert(memory.reg(AC) == 25)
//  }
//
//  test("sub test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 12
//    memory.reg(AC) = 13
//
//    spyUnit.sub()
//    assert(memory.reg(AC) == 1)
//  }
//
//  test("loop test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 1
//    spyUnit.loop()
//
//    assert(memory.reg(IP) == 1)
//
//    memory.reg(DR) = 0
//    spyUnit.loop()
//
//    assert(memory.reg(IP) == 1)
//  }
//
//  test("ld test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 42
//
//    spyUnit.ld()
//
//    assert(memory.reg(AC) == 42)
//  }
//
//  test("st test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = 42
//
//    spyUnit.st()
//
//    assert(memory.mem(0) == 42)
//  }
//
//  test("jump test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 0x6128
//
//    spyUnit.jump()
//
//    assert(memory.reg(IP) == 0x128)
//  }
//
//  test("jz test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(DR) = 0x6128
//
//    spyUnit.jz()
//
//    assert(memory.reg(IP) == 0)
//
//    memory.zero = true
//    memory.reg(DR) = 0x7043
//
//    spyUnit.jz()
//
//    assert(memory.reg(IP) == 0x43)
//  }
//
//  test("hlt test") {
//    assertThrows[HLTException] {
//      unit.hlt()
//    }
//  }
//
//  test("cla test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = 42
//
//    spyUnit.cla()
//
//    assert(memory.reg(AC) == 0)
//  }
//
//  test("inc test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = 42
//
//    spyUnit.inc()
//
//    assert(memory.reg(AC) == 43)
//  }
//
//  test("dec test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = 42
//
//    spyUnit.dec()
//
//    assert(memory.reg(AC) == 41)
//  }
//
//  test("output test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.mem(0) = 0
//    memory.mem(1) = 1
//    memory.mem(2) = 2
//    memory.mem(3) = 3
//
//    spyUnit.out()
//    spyUnit.input(0)
//    spyUnit.out()
//    spyUnit.input(0)
//    spyUnit.out()
//    spyUnit.input(0)
//    spyUnit.out()
//    spyUnit.input(0)
//
//    assert(unit.buffer == List(0, 1, 2, 3))
//  }
//
//  test("ZR test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = 12
//    memory.reg(DR) = 6
//
//    spyUnit.sub()
//    assert(memory.reg(ZR) == 0)
//
//    spyUnit.sub()
//    assert(memory.reg(ZR) == 1)
//  }
//
//  test("fix value test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(AC) = Memory.MAX_WORD
//    spyUnit.inc()
//    assert(memory.reg(AC) == Memory.MIN_WORD)
//
//    memory.reg(AC) = Memory.MIN_WORD
//    spyUnit.dec()
//    assert(memory.reg(AC) == Memory.MAX_WORD)
//  }
//
//  test("fix address test") {
//    val spyUnit = spy(unit)
//    doNothing().when(spyUnit).commandFetch()
//
//    memory.reg(IP) = Memory.MAX_ADDR
//    spyUnit.input(0)
//
//    assert(memory.reg(IP) == 0)
//  }
//
//}