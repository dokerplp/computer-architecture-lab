package machine

import exception.{HLTException, IllegalAddressException, IllegalDataFormatException}

import java.util.zip.DataFormatException
import scala.math.*
import scala.util.matching.Regex
import machine.ControlUnit.*

import machine.Memory.DataRegister._
import machine.Memory.AddrRegister._
import machine.AddressedCommand._
import machine.UnaddressedCommand._
class ControlUnit(private val tg: TactGenerator, private val memory: Memory, private val device: Device):
//  private var _log: List[Map[Memory.Register, Int]] = List()
//  def log: List[Map[Register, Int]] = _log
//  def freeLog(): Unit =
//    _log = List()
//  private def logEntry(): Unit =
//    _log = _log :+ (Map() ++ memory.registers)
//
//  private var _buffer: List[Int] = List()

//  def buffer: List[Int] =
//    val cp = _buffer
//    _buffer = List()
//    cp

  def logEntry(): Unit =
    println()


  def writeIP(): Unit =
    memory.reg(IP) = device.IO
    tg.tick()

  def input(): Unit =
    memory.reg(DR) = device.IO
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg ++ IP
    memory.mem(memory.reg(AR)) = memory.reg(DR)
    tg.tick()

  def loadWithDR(): Unit =
    memory.reg(AR) = memory.reg(DR)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()

  def loadWithIP(): Unit =
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()

  def operandFetch(): Unit =
    def absolute(): Unit =
      memory.reg(DR) = m11(memory.reg(DR))
      tg.tick()
      loadWithDR()

    if (bit(memory.reg(DR), 11) == 0) {
      absolute()
    } else if (bit(memory.reg(DR), 10) == 0) {
      memory.reg(DR) = m8(memory.reg(DR))
    } else {
      memory.reg(DR) = m8(memory.reg(DR))
      tg.tick()
      loadWithDR()
      absolute()
    }

  def commandFetch(): Unit =
    //stage 1: instr -> CR
    loadWithIP()
    memory.reg ++ IP
    memory.reg(CR) = memory.reg(DR)
    tg.tick()

    //stage 2: call command
    val hex: String = toHex(memory.reg(CR))
    //Unaddressed commands
    hex match
      case NULL.binary => _null()
      case HLT.binary  => hlt()
      case CLA.binary  => cla()
      case INC.binary => inc()
      case DEC.binary => dec()
      case _ => ()
      //case OUT.binary => out()

    //Addressed commands
    hex.charAt(0) match
      case ADD.binary => operandFetch(); add()
      case SUB.binary => operandFetch(); sub()
      case LOOP.binary => operandFetch(); loop()
      case LD.binary => operandFetch(); ld()
      case ST.binary => st()
      case JUMP.binary => jump()
      case JZ.binary => jz()

  //ADD -> 0x1xxx
  def add(): Unit =
    (memory.reg +++ AC)(memory.reg(DR))
    tg.tick()

    logEntry()
    commandFetch();


  //SUB -> 0x2xxx
  def sub(): Unit =
    (memory.reg --- AC)(memory.reg(DR))
    tg.tick()

    logEntry()
    commandFetch()

  //LOOP -> 0x3xxx
  def loop(): Unit =
    if (memory.reg(DR) > 0) {
      memory.reg ++ IP
      tg.tick()
    }

    logEntry()
    commandFetch()

  //ADD -> 0x4xxx
  def ld(): Unit =
    memory.reg(AC) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  //ST -> 0x5xxx
  def st(): Unit =
    memory.reg(DR) = m11(memory.reg(DR))
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
    memory.reg(DR) = m11(memory.reg(DR))
    memory.reg(IP) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  //JZ -> 0x7xxx
  def jz(): Unit =
    if (memory.zero) jump()
    logEntry()

  //NULL -> 0xF000
  def _null(): Unit =
    logEntry()
    commandFetch()

  //HLT -> 0xF100
  def hlt(): Unit =
    logEntry()
    throw new HLTException("Program finished by HLT instruction")

  //CLA -> 0xF200
  def cla(): Unit =
    memory.reg(AC) = 0
    tg.tick()

    logEntry()
    commandFetch()

  //INC -> 0xF300
  def inc(): Unit =
    memory.reg ++ AC
    tg.tick()

    logEntry()
    commandFetch()

  //DEC -> 0xF400
  def dec(): Unit =
    memory.reg -- AC
    tg.tick()

    logEntry()
    commandFetch()

  //OUT -> 0xF500
//  def out(): Unit =
//    memory.reg(IO) = memory.reg(AC)
//    _buffer = _buffer :+ memory.reg(IO)
//
//    logEntry()
//    commandFetch()

object ControlUnit:

  private def bit(x: Int, n: Int) = (x << n) & 1
  def m8(x: Int): Int = x & 0x00FF
  def m11(x: Int): Int = x & 0x07FF
  def m16(x: Int): Int = x & 0xFFFF

  def toHex(x: Int): String = m16(x).toHexString.toUpperCase
