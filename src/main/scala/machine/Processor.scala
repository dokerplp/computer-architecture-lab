package machine

import Memory.AddrRegister._
import Memory.DataRegister._

import scala.annotation.tailrec

private class Processor(device: Device) {
  val memory: Memory = new Memory
  val tg: TactGenerator = new TactGenerator
  val controlUnit = new ControlUnit(tg, memory, device)
  
//  def log: String = {
//    @tailrec
//    def zeros(s: String): String =
//      if (s.length < 4) zeros("0" + s)
//      else s
//
//    def entry(map: Map[Memory.Register, Int]): String = {
//      val hex = map
//        .map(e => (e._1, ControlUnit.mask(e._2)))
//        .map(e => (e._1, zeros(e._2.toHexString.toUpperCase)))
//      s"|${hex(AC)}|${hex(ZR)}|${hex(DR)}|${hex(IP)}|${hex(CR)}|${hex(AR)}|\n"
//    }
//    val head = "| AC | ZR | DR | IP | CR | AR |\n"
//    val sb = new StringBuilder
//    sb.append(head)
//    controlUnit.log.foreach(map => sb.append(entry(map)))
//    sb.toString
//  }

  def startProgram(ip: Int): Unit =
    //controlUnit.freeLog()
    device.IO = ip
    controlUnit.writeIP()
    controlUnit.commandFetch()
}
