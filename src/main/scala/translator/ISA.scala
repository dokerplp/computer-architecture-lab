package translator

import exception.HLTException
import machine.AddressedCommand
import machine.AddressedCommand.Addressing.*
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
import scala.util.control.Exception.ignoring

class ISA:
  private val _user = new User
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
   * Get user
   */
  def user: User = _user

  /**
   * Translates pseudo-assembler and executes it
   * @param as - assembler source file
   * @param in - input buffer source file
   * @param out - output buffer target file
   * @param log - output logs file
   * @param str - is the value of the buffer a string
   */
  def translate(as: String, in: String, out: String, log: String, str: Boolean = false): Unit =
    //sources
    val asSrc = Source.fromFile(as)
    val inSrc = Source.fromFile(in)

    val lines: List[String] = asSrc.getLines().toList
    val input: List[Int] = inSrc.getLines().toList.mkString.chars().toArray.toList :+ 0

    //initialize label "start" if missing
    labels("start") = 0
    //read all labels in file and set their addresses
    setLabels(lines, 0)
    //translate assembler
    val instr = parse(lines, List.empty)
    //run translated program
    val output = start(instr, input, str)

    Files.write(Paths.get(log), _user.processor.log.getBytes(StandardCharsets.UTF_8))
    Files.write(Paths.get(out), output.getBytes(StandardCharsets.UTF_8))

    asSrc.close
    inSrc.close

  /**
   * Start program execution
   * @param instr - program instructions
   * @param input - input buffer
   * @param str - is the value of the buffer a string
   * @return
   */
  private def start(instr: List[Int], input: List[Int], str: Boolean): String =
    _user.device.input = input
    _user.load(instr, addr)

    val start = labels("start")
    ignoring(classOf[HLTException]) { _user.run(start) }
    val output = _user.device.output

    if (str) output.map(i => i.toChar).mkString else output.mkString(" ")

  /**
   * Mnemonic to binary format
   */
  private def toBinary(com: String, arg: String): Int =
    val ad = AddressedCommand.find(com)
    if (ad.isDefined) arg match
      case absoluteRegex(l) => ad.get(labels(l), ABSOLUTE)
      case directRegex(l) => ad.get(l.toInt, DIRECT)
      case relativeRegex(l) => ad.get(labels(l), RELATIVE)
    else throw new IllegalArgumentException(s"The unknown instruction \"$com\"")

  /**
   * Read all labels in file and set their addresses
   */
  @tailrec
  private def setLabels(lines: List[String], address: Int): Unit =
    if (lines.nonEmpty) lines.head match
        case addressedCommandRegex(_, org, arg) if org == "ORG" =>
          addr = hex(arg)
          setLabels(lines.tail, addr)
        case labelRegex(l) =>
          labels(l) = address
          setLabels(lines.tail, address + 1)
        case _ =>
          setLabels(lines.tail, address + 1)

  /**
   * Translate each instruction
   */
  @tailrec
  private def parse(lines: List[String], instructions: List[Int]): List[Int] =
    if (lines.nonEmpty) lines.head match
        case addressedCommandRegex(_, com, arg) if com != "ORG" =>
          parse(lines.tail, instructions :+ toBinary(com, arg))
        case unaddressedCommandOrDataRegex(_, com) =>
          val un = UnaddressedCommand.find(com)
          if (un.isDefined) parse(lines.tail, instructions :+ un.get.bin)
          else if (hexRegex.matches(com)) parse(lines.tail, instructions :+ hex(com))
          else parse(lines.tail, instructions :+ labels(com))
        case _ => parse(lines.tail, instructions)
    else instructions






