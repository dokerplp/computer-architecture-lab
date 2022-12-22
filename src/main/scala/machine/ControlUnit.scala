package machine

import exception.HLTException
import machine.AddressedCommand.*
import machine.Memory.AddrRegister
import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister
import machine.Memory.DataRegister.*
import machine.UnaddressedCommand.*
import util.Binary.*

import java.util.zip.DataFormatException
import scala.math.*
import scala.util.matching.Regex

class ControlUnit(private val tg: TactGenerator, private val memory: Memory, private val device: Device):
  type Tick = Int
  type DataRegs = Map[DataRegister, Int]
  type AddrRegs = Map[AddrRegister, Int]
  type Log = (Tick, DataRegs, AddrRegs)

  private val mem: memory.Mem = memory.mem
  private val reg: memory.Reg = memory.reg

  private var _log: List[Log] = List.empty

  def log: List[Log] = _log

  def freeLog(): Unit =
    _log = List.empty
  
  def writeIP(): Unit =
    reg(IP) = device.IO
    tg.tick()

  def input(): Unit =
    reg(DR) = device.IO
    reg(AR) = reg(IP)
    tg.tick()
    reg ++ IP
    mem(reg(AR)) = reg(DR)
    tg.tick()

  def operandFetch(): Unit =
    def absolute(): Unit =
      reg(DR) = m11(reg(DR))
      tg.tick()
      loadWithDR()

    if (bit(reg(DR), 11) == 0) absolute()
    else if (bit(reg(DR), 10) == 0) {
      reg(DR) = m8(reg(DR))
      tg.tick()
    } else {
      reg(DR) = m8(reg(DR))
      tg.tick()
      loadWithDR()
      absolute()
    }

  def commandFetch(): Unit =
    //stage 1: instr -> CR
    loadWithIP()
    reg ++ IP
    reg(CR) = reg(DR)
    tg.tick()

    //stage 2: call command
    val instr: String = hex(reg(CR))
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
    reg.add(AC, reg(DR))
    tg.tick()

    logEntry()
    commandFetch()

  def sub(): Unit =
    reg.sub(AC, reg(DR))
    tg.tick()

    logEntry()
    commandFetch()

  def loop(): Unit =
    if (reg(DR) > 0) reg ++ IP
    tg.tick()

    logEntry()
    commandFetch()

  def ld(): Unit =
    reg(AC) = reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  def st(): Unit =
    reg(DR) = m11(reg(DR))
    tg.tick()
    reg(AR) = reg(DR)
    reg(DR) = reg(AC)
    tg.tick()
    mem(reg(AR)) = reg(DR)
    tg.tick()

    logEntry()
    commandFetch()

  def jump(): Unit =
    reg(DR) = m11(reg(DR))
    reg(IP) = reg(DR)
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
    reg(AC) = 0
    tg.tick()

    logEntry()
    commandFetch()

  def inc(): Unit =
    reg ++ AC
    tg.tick()

    logEntry()
    commandFetch()

  def dec(): Unit =
    reg -- AC
    tg.tick()

    logEntry()
    commandFetch()

  def out(): Unit =
    device.IO = reg(AC)
    device.write()
    tg.tick()

    logEntry()
    commandFetch()

  def in(): Unit =
    device.read()
    reg(AC) = device.IO
    tg.tick()

    logEntry()
    commandFetch()

  private def logEntry(): Unit =
    _log = _log :+ (tg.tact, memory.dataRegs, memory.addrRegs)

  private def loadWithDR(): Unit =
    reg(AR) = reg(DR)
    tg.tick()
    reg(DR) = mem(reg(AR))
    tg.tick()

  private def loadWithIP(): Unit =
    reg(AR) = reg(IP)
    tg.tick()
    reg(DR) = mem(reg(AR))
    tg.tick()
