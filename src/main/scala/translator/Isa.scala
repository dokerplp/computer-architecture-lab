package translator

import exception.HLTException
import machine.AddressedCommand.Type.*
import machine.{AddressedCommand, UnaddressedCommand, User}
import util.Binary.hex

import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer as MutableList, Map as MutableMap}
import scala.io.Source

class Isa:
  private val user: User = new User
  private val labels: MutableMap[String, Int] = MutableMap()
  private val labelRegex = """(\w+):.*""".r
  private val addressedCommandRegex = """(\w+:\s+)?(\w+)\s+([$#()\w]+)""".r
  private val unaddressedCommandOrDataRegex = """(\w+:\s+)?(\w+)""".r
  private val absoluteRegex = """\$(\w+)""".r
  private val directRegex = """#(\w+)""".r
  private val relativeRegex = """\((\w+)\)""".r
  private val hexRegex = """[0-9A-F]+""".r
  private var addr = 0

  def translate(input: String, output: String): Unit =
    val src = Source.fromFile(input)
    val lines: List[String] = src.getLines().toList

    setLabels(lines, 0)
    val instructions = parse(lines, List())

    user.load(instructions, addr)
    try {
      val start = labels("start")
      user.run(start)
    } catch {
      case e: HLTException => println(e.getMessage)
    }
    println(user.processor.log)
    println(user.device.output.map(i => i.toChar).mkString)

  private def toBinary(com: String, arg: String): Int =
    val ad = AddressedCommand.parse(com)
    if (ad.isDefined) arg match
      case absoluteRegex(l) => ad.get.toBinary(ABSOLUTE, labels(l))
      case directRegex(l) => ad.get.toBinary(DIRECT, labels(l))
      case relativeRegex(l) => ad.get.toBinary(RELATIVE, labels(l))
    else throw new RuntimeException()

  @tailrec
  private def setLabels(lines: List[String], address: Int): Unit =
    if (lines.nonEmpty) {
      lines.head match
        case addressedCommandRegex("ORG", arg) =>
          addr = hex(arg)
          setLabels(lines.tail, addr)
        case labelRegex(l) =>
          labels(l) = address
          setLabels(lines.tail, address + 1)
        case _ =>
          setLabels(lines.tail, address + 1)
    }

  @tailrec
  private def parse(lines: List[String], instructions: List[Int]): List[Int] =
    if (lines.isEmpty) return instructions
    lines.head match
      case addressedCommandRegex(_, com, arg) if com != "ORG" =>
        parse(lines.tail, instructions :+ toBinary(com, arg))
      case unaddressedCommandOrDataRegex(_, com) =>
        val un = UnaddressedCommand.parse(com)
        if (un.isDefined) parse(lines.tail, instructions :+ un.get.toBinary)
        else if (hexRegex.matches(com)) parse(lines.tail, instructions :+ hex(com))
        else parse(lines.tail, instructions :+ labels(com))
      case _ => parse(lines.tail, instructions)






