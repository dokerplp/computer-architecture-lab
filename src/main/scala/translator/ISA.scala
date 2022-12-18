package translator

import exception.HLTException
import machine.AddressedCommand
import machine.AddressedCommand.Type.*
import machine.UnaddressedCommand
import machine.User
import util.Binary.hex

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer => MutableList}
import scala.collection.mutable.{Map => MutableMap}
import scala.io.Source

class ISA:
  private val user = new User
  private val labels: MutableMap[String, Int] = MutableMap()
  private val labelRegex = """(\w+):.*""".r
  private val addressedCommandRegex = """(\w+:\s+)?(\w+)\s+([$#()\w]+)""".r
  private val unaddressedCommandOrDataRegex = """(\w+:\s+)?(\w+)""".r
  private val absoluteRegex = """\$(\w+)""".r
  private val directRegex = """#(\w+)""".r
  private val relativeRegex = """\((\w+)\)""".r
  private val hexRegex = """[0-9A-F]+""".r
  private var addr = 0

  /**
   * Translation and execution of assembler
   *
   * @param as  - assembler source
   * @param in  - input data
   * @param out - output data
   * @param log - program log
   * @param str - result is string or numbers
   */
  def translate(as: String, in: String, out: String, log: String, str: Boolean = false): Unit =
    val src = Source.fromFile(as)
    val lines: List[String] = src.getLines().toList

    val inSrc = Source.fromFile(in)
    val input: List[Int] = inSrc.getLines().toList.mkString.chars().toArray.toList :+ 0

    setLabels(lines, 0)
    val instructions = parse(lines, List.empty)

    user.device.input = input
    user.load(instructions, addr)
    try {
      val start = labels("start")
      user.run(start)
    } catch {
      case e: HLTException => println(e.getMessage)
    }

    val res = if (str) user.device.output.map(i => i.toChar).mkString else user.device.output.mkString(" ")
    Files.write(Paths.get(log), user.processor.log.getBytes(StandardCharsets.UTF_8))
    Files.write(Paths.get(out), res.getBytes(StandardCharsets.UTF_8))
    src.close
    inSrc.close

  /**
   * Parse assembler code to binary format
   *
   * @param com - command name
   * @param arg - argument
   * @return command in binary command
   */
  private def toBinary(com: String, arg: String): Int =
    val ad = AddressedCommand.parse(com)
    if (ad.isDefined) arg match
      case absoluteRegex(l) => ad.get.toBinary(ABSOLUTE, labels(l))
      case directRegex(l) => ad.get.toBinary(DIRECT, l.toInt)
      case relativeRegex(l) => ad.get.toBinary(RELATIVE, labels(l))
    else throw new RuntimeException()

  /**
   * Parse labels and set addresses for them
   *
   * @param lines   - assembler lines
   * @param address - current address
   */
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

  /**
   * Parse each assembler line
   *
   * @param lines        - assembler lines
   * @param instructions - parsing result
   * @return instructions
   */
  @tailrec
  private def parse(lines: List[String], instructions: List[Int]): List[Int] =
    if (lines.nonEmpty) {
      lines.head match
        case addressedCommandRegex(_, com, arg) if com != "ORG" =>
          parse(lines.tail, instructions :+ toBinary(com, arg))
        case unaddressedCommandOrDataRegex(_, com) =>
          val un = UnaddressedCommand.parse(com)
          if (un.isDefined) parse(lines.tail, instructions :+ un.get.toBinary)
          else if (hexRegex.matches(com)) parse(lines.tail, instructions :+ hex(com))
          else parse(lines.tail, instructions :+ labels(com))
        case _ => parse(lines.tail, instructions)
    } else instructions






