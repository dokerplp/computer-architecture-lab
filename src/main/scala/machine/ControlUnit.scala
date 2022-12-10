package machine

import exception.{HLTException, IllegalAddressException}
import machine.Memory.Register
import machine.Memory.Register.*

import java.util.zip.DataFormatException
import scala.math.*
class ControlUnit(private val tg: TactGenerator, private val memory: Memory):
  def writeIP(addr: Int): Unit =
    if (addr < 0 || addr > memory.maxAddr) throw new IllegalAddressException(s"Stack doesn't have address $addr")
    memory.reg(IN) = addr
    tg.tick()
    memory.reg(IP) = memory.reg(IN)
    tg.tick()

  def input(data: Int): Unit =
    if (data < memory.minWord || data > memory.maxWord) throw new DataFormatException(s"$data doesn't match word size")
    memory.reg(IN) = data
    tg.tick()
    memory.reg(DR) = memory.reg(IN)
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg ++ IP
    fixAddrReg(IP)
    memory.mem(memory.reg(AR)) = memory.reg(DR)
    tg.tick()

  def getAddr(instr: Int): Int =
    instr & 0x0FFF // word size is hardcoded, word size is 16 bit, first 4 bit - command, last 12 bit - address
  def operandFetch(): Unit =
    memory.reg(DR) = getAddr(memory.reg(DR))
    tg.tick()
    memory.reg(AR) = memory.reg(DR)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    tg.tick()

  private val wordPattern = """^(\w)(\w)(\w)(\w)$""".r
  def commandFetch(): Unit =
    //stage 1: instr -> CR
    memory.reg(AR) = memory.reg(IP)
    tg.tick()
    memory.reg(DR) = memory.mem(memory.reg(AR))
    memory.reg ++ IP
    fixAddrReg(IP)
    tg.tick()
    memory.reg(CR) = memory.reg(DR)
    tg.tick()

    //stage 2: call command
    val hex = memory.reg(CR).toHexString.toUpperCase
    hex match {
      case wordPattern("1", _, _, _) => operandFetch(); add()
      case wordPattern("2", _, _, _) => operandFetch(); sub()
      case wordPattern("3", _, _, _) => operandFetch(); loop()
      case wordPattern("4", _, _, _) => operandFetch(); ld()
      case wordPattern("5", _, _, _) => st()
      case wordPattern("6", _, _, _) => jump()
      case wordPattern("7", _, _, _) => jz()
      case "F100" => hlt()
      case "F200" => cla()
      case "F300" => inc()
      case "F400" => dec()
      case "F500" => output()
    }

  def fixValReg(reg: Register): Unit =
    if (memory.reg(reg) > memory.maxWord)
      memory.reg(reg) = memory.minWord + memory.reg(reg) - memory.maxWord - 1
    else if (memory.reg(reg) < memory.minWord)
      memory.reg(reg) = memory.maxWord + memory.reg(reg) - memory.minWord + 1

  def fixAddrReg(reg: Register): Unit =
    if (memory.reg(reg) > memory.maxWord)
      memory.reg(reg) = memory.reg(reg) - memory.maxWord - 1
    else if (memory.reg(reg) < memory.minWord)
      memory.reg(reg) = memory.reg(reg) - memory.minWord + 1

  def setZR(): Unit =
    memory.reg(ZR) = if (memory.reg(AC) == 0) 1 else 0

  //ADD -> 0x1xxx
  def add(): Unit =
    (memory.reg +++ AC)(memory.reg(DR))
    fixValReg(AC)
    setZR()
    tg.tick()

    commandFetch();


  //SUB -> 0x2xxx
  def sub(): Unit =
    (memory.reg --- AC)(memory.reg(DR))
    fixValReg(AC)
    setZR()
    tg.tick()

    commandFetch()

  //LOOP -> 0x3xxx
  def loop(): Unit =
    if (memory.reg(DR) > 0) {
      memory.reg ++ IP
      fixAddrReg(IP)
      tg.tick()
    } else {
      memory.mem(memory.reg(AR)) = memory.reg(DR) - 1
      tg.tick()
    }

    commandFetch()

  //ADD -> 0x4xxx
  def ld(): Unit =
    memory.reg(AC) = memory.reg(DR)
    setZR()
    tg.tick()

    commandFetch()

  //ST -> 0x5xxx
  def st(): Unit =
    memory.reg(DR) = getAddr(memory.reg(DR))
    tg.tick()
    memory.reg(AR) = memory.reg(DR)
    tg.tick()
    memory.reg(DR) = memory.reg(AC)
    tg.tick()
    memory.mem(memory.reg(AR)) = memory.reg(DR)
    tg.tick()

    commandFetch()

  //JUMP -> 0x6xxx
  def jump(): Unit =
    memory.reg(DR) = getAddr(memory.reg(DR))
    tg.tick()
    memory.reg(IP) = memory.reg(DR)
    tg.tick()

    commandFetch()

  //JZ -> 0x7xxx
  def jz(): Unit =
    if (memory.reg(ZR) == 1) jump()

  //HLT -> 0xF100
  def hlt(): Unit = throw new HLTException("Program finished by HLT instruction")

  //CLA -> 0xF200
  def cla(): Unit =
    memory.reg(AC) = 0
    memory.reg(ZR) = 1
    tg.tick()

    commandFetch()

  //INC -> 0xF300
  def inc(): Unit =
    memory.reg ++ AC
    fixValReg(AC)
    setZR()
    tg.tick()

    commandFetch()

  //DEC -> 0xF400
  def dec(): Unit =
    memory.reg -- AC
    fixValReg(AC)
    setZR()
    tg.tick()

    commandFetch()

  //OUT -> 0xF500
  def output(): Unit =
    memory.buffer.addOne(memory.mem(memory.reg(IP)))

    commandFetch()





