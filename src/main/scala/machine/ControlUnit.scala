package machine

import exception.{HLTException, IllegalAddressException, IllegalDataFormatException}
import machine.Memory.Register
import machine.Memory.Register.*

import java.util.zip.DataFormatException
import scala.math.*
import scala.util.matching.Regex
import ControlUnit.AddressCommands.*
import ControlUnit.UnaddressedCommands.*
import machine.ControlUnit.*
class ControlUnit(private val tg: TactGenerator, private val memory: Memory):
  private var _log: List[Map[Memory.Register, Int]] = List()
  def log: List[Map[Register, Int]] = _log
  def freeLog(): Unit =
    _log = List()
  private def logEntry(): Unit =
    _log = _log :+ (Map() ++ memory.registers)

  private var _buffer: List[Int] = List()

  def buffer: List[Int] =
    val cp = _buffer
    _buffer = List()
    cp



  def writeIP(addr: Int): Unit =
    if (addr < 0 || addr > Memory.MAX_ADDR) throw new IllegalAddressException(s"Stack doesn't have address $addr")
    memory.reg(IO) = addr
    tg.tick()
    memory.reg(IP) = memory.reg(IO)
    tg.tick()

  def input(data: Int): Unit =
    if (data < Memory.MIN_WORD || data > Memory.MAX_WORD) throw new IllegalDataFormatException(s"$data doesn't match word size")
    memory.reg(IO) = data
    tg.tick()
    memory.reg(DR) = memory.reg(IO)
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg ++ IP
    memory.reg(IP) = fixAddr(memory.reg(IP))
    memory.mem(memory.reg(AR)) = memory.reg(DR)
    tg.tick()

  def operandFetch(): Unit =
    memory.reg(DR) = addr(memory.reg(DR))
    tg.tick()
    memory.reg(AR) = memory.reg(DR)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()

  def commandFetch(): Unit =
    //stage 1: instr -> CR
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    memory.reg ++ IP
    memory.reg(IP) = fixAddr(memory.reg(IP))
    tg.tick()
    memory.reg(CR) = memory.reg(DR)
    tg.tick()

    //stage 2: call command
    val hex = mask(memory.reg(CR)).toHexString.toUpperCase
    hex match {
      case ADD.r() => operandFetch(); add()
      case SUB.r() => operandFetch(); sub()
      case LOOP.r() => operandFetch(); loop()
      case LD.r() => operandFetch(); ld()
      case ST.r() => st()
      case JUMP.r() => jump()
      case JZ.r() => jz()
      case HLT.com => hlt()
      case CLA.com => cla()
      case INC.com => inc()
      case DEC.com => dec()
      case OUT.com => out()
    }

  private def setZR(): Unit =
    memory.reg(ZR) = if (memory.reg(AC) == 0) 1 else 0

  //ADD -> 0x1xxx
  def add(): Unit =
    (memory.reg +++ AC)(memory.reg(DR))
    memory.reg(AC) = fixVal(memory.reg(AC))
    setZR()
    tg.tick()

    logEntry()
    commandFetch();


  //SUB -> 0x2xxx
  def sub(): Unit =
    (memory.reg --- AC)(memory.reg(DR))
    memory.reg(AC) = fixVal(memory.reg(AC))
    setZR()
    tg.tick()

    logEntry()
    commandFetch()

  //LOOP -> 0x3xxx
  def loop(): Unit =
    if (memory.reg(DR) > 0) {
      memory.reg ++ IP
      memory.reg(IP) = fixAddr(memory.reg(IP))
      tg.tick()
    }

    logEntry()
    commandFetch()

  //ADD -> 0x4xxx
  def ld(): Unit =
    memory.reg(AC) = memory.reg(DR)
    setZR()
    tg.tick()

    logEntry()
    commandFetch()

  //ST -> 0x5xxx
  def st(): Unit =
    memory.reg(DR) = addr(memory.reg(DR))
    tg.tick()
    memory.reg(AR) = memory.reg(DR)
    memory.reg(DR) = memory.reg(AC)
    tg.tick()
    memory.mem(memory.reg(AR)) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  //JUMP -> 0x6xxx
  def jump(): Unit =
    memory.reg(DR) = addr(memory.reg(DR))
    memory.reg(IP) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  //JZ -> 0x7xxx
  def jz(): Unit =
    if (memory.reg(ZR) == 1) jump()
    logEntry()

  //HLT -> 0xF100
  def hlt(): Unit =
    logEntry()
    throw new HLTException("Program finished by HLT instruction")

  //CLA -> 0xF200
  def cla(): Unit =
    memory.reg(AC) = 0
    memory.reg(ZR) = 1
    tg.tick()

    logEntry()
    commandFetch()

  //INC -> 0xF300
  def inc(): Unit =
    memory.reg ++ AC
    memory.reg(AC) = fixVal(memory.reg(AC))
    setZR()
    tg.tick()

    logEntry()
    commandFetch()

  //DEC -> 0xF400
  def dec(): Unit =
    memory.reg -- AC
    memory.reg(AC) = fixVal(memory.reg(AC))
    setZR()
    tg.tick()

    logEntry()
    commandFetch()

  //OUT -> 0xF500
  def out(): Unit =
    memory.reg(IO) = memory.reg(AC)
    _buffer = _buffer :+ memory.reg(IO)

    logEntry()
    commandFetch()

object ControlUnit:
  def addr(instr: Int): Int = instr & 0x0FFF
  def mask(x: Int): Int = x & 0x0000FFFF

  private def fixVal(x: Int): Int =
    if (x > Memory.MAX_WORD)
      Memory.MIN_WORD + x - Memory.MAX_WORD - 1
    else if (x < Memory.MIN_WORD)
      Memory.MAX_WORD + x - Memory.MIN_WORD + 1
    else x

  private def fixAddr(x: Int): Int =
    if (x > Memory.MAX_ADDR)
      x - Memory.MAX_ADDR - 1
    else if (x < 0)
      Memory.MAX_ADDR + x + 1
    else x

  enum AddressCommands(val r: Regex, val com: String):
    case ADD extends AddressCommands("""1\w\w\w""".r, "1")
    case SUB extends AddressCommands("""2\w\w\w""".r, "2")
    case LOOP extends AddressCommands("""3\w\w\w""".r, "3")
    case LD extends AddressCommands("""4\w\w\w""".r, "4")
    case ST extends AddressCommands("""5\w\w\w""".r, "5")
    case JUMP extends AddressCommands("""6\w\w\w""".r, "6")
    case JZ extends AddressCommands("""7\w\w\w""".r, "7")

    def apply(addr: Int): Int = fixVal(Integer.parseInt(s"${com}000", 16) + addr)

  enum UnaddressedCommands(val com: String):
    case HLT extends UnaddressedCommands("F100")
    case CLA extends UnaddressedCommands("F200")
    case INC extends UnaddressedCommands("F300")
    case DEC extends UnaddressedCommands("F400")
    case OUT extends UnaddressedCommands("F500")

    def apply(): Int = fixVal(Integer.parseInt(com, 16))