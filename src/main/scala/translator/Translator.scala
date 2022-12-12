package translator

import exception.TranslationException

import scala.annotation.tailrec
import scala.io.Source
import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import machine.ControlUnit.AddressCommands.*
import machine.ControlUnit.UnaddressedCommands.*

import scala.collection.mutable

class Translator:
  private val ints: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val addrs: MutableMap[String, Int] = MutableMap()
  private val prog: MutableList[Int] = MutableList()

  private val intRegex = """\s*int\s+([a-zA-Z]+)\s*=\s*(.+)\s*""".r
  private val intValueRegex = """\s*(-?[1-9]\d*)\s*""".r
  private val varValueRegex = """\s*([a-zA-Z]+)\s*""".r
  private val expressionRegex = """\s*(\w+)\s*([+-])\s*(.+)\s*""".r
  private val stringRegex = """\s*string\s+([a-zA-Z]+)\s*=\s*\"([a-zA-Z]+)\"\s*""".r
  private val changeVarRegex = """\s*([a-zA-Z]+)\s*=\s*(.+)\s*""".r
  private val whileRegex = """\s*while\s*\(([a-zA-Z]+)\)\s*\{\s*""".r

  final def expressionParser(expression: String, num: Int, com: Boolean): Int =
    def instr(name: String, sign: Int): Unit =
      if (sign > 0) prog.addOne(ADD(addrs(name)))
      else if (sign < 0) prog.addOne(SUB(addrs(name)))
    def instrDirect(value: Int, sign: Int): Unit =
      if (sign > 0) prog.addOne(ADD.direct(value))
      else if (sign < 0) prog.addOne(SUB.direct(value))
    @tailrec
    def helper(expression: String, acc: Int, sign: Int): Int =
      expression match
        case intValueRegex(value) =>
          val numOpt = value.toIntOption
          if (numOpt.isEmpty) throw new TranslationException(s"Line $num: can't cast variable \"$value\" to integer type.")
          if (com) instrDirect(numOpt.get, sign)
          acc + sign * numOpt.get
        case varValueRegex(value) =>
          if (!ints.contains(value)) throw new TranslationException(s"Line $num: can't find variable \"$value\".")
          if (com) instr(value, sign)
          acc + sign * ints(value)
        case expressionRegex(name, op, expr) =>
          val number = {
            val numOpt = name.toIntOption
            if (numOpt.isDefined) {
              if (com) instrDirect(numOpt.get, sign)
              numOpt.get
            }
            else if (ints.contains(name)) {
              if (com) instr(name, sign)
              ints(name)
            }
            else throw new TranslationException(s"Line $num: can't cast variable \"$name\" to integer type.")
          }
          val newSign = if (op == "-") -1 else 1
          helper(expr, acc + sign * number, newSign)
        case _ => throw new TranslationException(s"Line $num: can't parse \"$expression\"")
    helper(expression, 0, 1)

  //set variables
  @tailrec
  final def translationStage1(lines: List[String], num: Int): Unit =
    if (lines.nonEmpty)
      lines.head match
        case stringRegex(name, value) =>
          if (!strings.contains(name)) strings(name) = value
          else throw new TranslationException(s"Line $num: double declaration of variable $name")
        case intRegex(name, expr) =>
          if (!ints.contains(name)) ints(name) = expressionParser(expr, num, false)
          else throw new TranslationException(s"Line $num: double declaration of variable $name")
        case _ => ()
      translationStage1(lines.tail, num + 1)

  def translationStage2(): Unit =
    for (v <- ints) {
      addrs(v._1) = prog.length
      prog.addOne(v._2)
    }
    for (v <- strings) {
      addrs(v._1) = prog.length
      for (c <- v._2) prog.addOne(c.toInt)
      prog.addOne(0)
    }

  def translationStage3(lines: List[String], num: Int): Unit =
    def checkVariable(name: String): Unit =
      if (strings.contains(name)) throw new TranslationException(s"Line $num: strings can't be modified")
      if (!ints.contains(name)) throw new TranslationException(s"Line $num: undefined variable \"$name\"")

    @tailrec
    def helper(lines: List[String], loop: mutable.Stack[Int]): Unit =
      if (lines.nonEmpty)
        lines.head match
          case stringRegex(name, value) => ()
          case intRegex(name, expr) => ()
          case changeVarRegex(name, expr) =>
            checkVariable(name)
            expressionParser(expr, num, true)
            prog.addOne(ST(addrs(name)))
          case whileRegex(name) =>
            checkVariable(name)
            prog.addOne(LOOP(addrs(name)))
            loop.push(prog.length - 1)
            prog.addOne(0)
          case "}" =>
            if (loop.isEmpty) throw TranslationException(s"Line $num: undefined symbol \"}\"")
            val addr = loop.pop()
            prog.addOne(JUMP(addr))
            prog(addr + 1) = JUMP(prog.length)
          case _ => throw TranslationException(s"Line $num: undefined line \"${lines.head}\"")
        helper(lines.tail, loop)

    helper(lines, mutable.Stack())



  def translate(input: String, output: String): Unit = {
    val src = Source.fromFile(input)
    val lines = src.getLines().toList
    translationStage1(lines, 1)
    translationStage2()
    translationStage3(lines, 1)


    src.close
  }

