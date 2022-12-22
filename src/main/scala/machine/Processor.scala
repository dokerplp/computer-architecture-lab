package machine

import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import util.Binary.hex
import util.Binary.m16

import scala.annotation.tailrec

private class Processor(device: Device):
  val memory: Memory = new Memory
  val tg: TactGenerator = new TactGenerator
  val controlUnit = new ControlUnit(tg, memory, device)

  def log: String = {
    @tailrec
    def zeros(s: String): String = if (s.length < 8) zeros("0" + s) else s

    def entry(en: controlUnit.Log): String =
      val tact = zeros(en._1.toHexString)
      val data = en._2.map(e => (e._1, zeros(hex(e._2))))
      val addr = en._3.map(e => (e._1, zeros(hex(e._2))))

      s"|$tact|${data(AC)}|${data(DR)}|${data(CR)}|${addr(IP)}|${addr(AR)}|\n"

    val head = "|  TACT  |   AC   |   DR   |   CR   |   IP   |   AR   |\n"
    val sb = new StringBuilder
    sb.append(head)
    controlUnit.log.foreach(map => sb.append(entry(map)))
    sb.toString
  }


  def startProgram(ip: Int): Unit =
    controlUnit.freeLog()
    device.IO = ip
    controlUnit.writeIP()
    tg.clean()
    controlUnit.commandFetch()

