package machine

import exception.HLTException
import machine.AddressedCommand.*
import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import machine.Memory.{AddrRegister, DataRegister}
import machine.UnaddressedCommand.*
import util.Binary.*

import java.util.zip.DataFormatException
import scala.math.*
import scala.util.matching.Regex

class ControlUnit(private val tg: TactGenerator, private val memory: Memory, private val device: Device):

  /**
   * Log in format (TICK, DATA REGISTERS, ADDRESS REGISTERS)
   */
  private var _log: List[(Int, Map[DataRegister, Int], Map[AddrRegister, Int])] = List()

  def log: List[(Int, Map[DataRegister, Int], Map[AddrRegister, Int])] = _log

  def freeLog(): Unit = _log = List()

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

  def operandFetch(): Unit =
    def absolute(): Unit =
      memory.reg(DR) = m11(memory.reg(DR))
      tg.tick()
      loadWithDR()

    if (bit(memory.reg(DR), 11) == 0) absolute()
    else if (bit(memory.reg(DR), 10) == 0) {
      memory.reg(DR) = m8(memory.reg(DR))
      tg.tick()
    }
    else {
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
    val instr: String = hex(memory.reg(CR))
    //Unaddressed commands
    instr match
      case NULL.binary => _null()
      case HLT.binary => hlt()
      case CLA.binary => cla()
      case INC.binary => inc()
      case DEC.binary => dec()
      case OUT.binary => out()
      case IN.binary => in()
      case _ => ()

    //Addressed commands
    instr.charAt(0) match
      case ADD.binary => operandFetch(); add()
      case SUB.binary => operandFetch(); sub()
      case LOOP.binary => operandFetch(); loop()
      case LD.binary => operandFetch(); ld()
      case ST.binary => st()
      case JUMP.binary => jump()
      case JZ.binary => jz()

  def add(): Unit =
    (memory.reg +++ AC)(memory.reg(DR))
    tg.tick()

    logEntry()
    commandFetch();

  def sub(): Unit =
    (memory.reg --- AC)(memory.reg(DR))
    tg.tick()

    logEntry()
    commandFetch()

  def loop(): Unit =
    if (memory.reg(DR) > 0) memory.reg ++ IP
    tg.tick()

    logEntry()
    commandFetch()

  def ld(): Unit =
    memory.reg(AC) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

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

  def jump(): Unit =
    memory.reg(DR) = m11(memory.reg(DR))
    memory.reg(IP) = memory.reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  def jz(): Unit =
    if (memory.zero) jump()
    else {
      logEntry()
      commandFetch()
    }

  def _null(): Unit =
    logEntry()
    commandFetch()

  def hlt(): Unit =
    logEntry()
    throw new HLTException("Program finished by HLT instruction")

  def cla(): Unit =
    memory.reg(AC) = 0
    tg.tick()

    logEntry()
    commandFetch()

  def inc(): Unit =
    memory.reg ++ AC
    tg.tick()

    logEntry()
    commandFetch()

  def dec(): Unit =
    memory.reg -- AC
    tg.tick()

    logEntry()
    commandFetch()

  def out(): Unit =
    device.IO = memory.reg(AC)
    device.write()
    tg.tick()

    logEntry()
    commandFetch()

  def in(): Unit =
    device.read()
    memory.reg(AC) = device.IO
    tg.tick()

    logEntry()
    commandFetch()

  /**
   * Add entry to log
   */
  private def logEntry(): Unit =
    _log = _log :+ (tg.tact, Map() ++ memory.dataRegisters, Map() ++ memory.addrRegisters)

  private def loadWithDR(): Unit =
    memory.reg(AR) = memory.reg(DR)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()

  private def loadWithIP(): Unit =
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()
