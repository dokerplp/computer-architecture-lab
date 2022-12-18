package translator

import exception.TranslationException
import machine.AddressedCommand.*
import machine.AddressedCommand.Type.*
import machine.UnaddressedCommand.*
import translator.Translator.*

import java.io.{File, FileReader, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer as MutableList, Map as MutableMap}
import scala.io.Source

class Translator:
  private val nums: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val loops: mutable.Stack[Int] = mutable.Stack()
  private val intRegex = """int\s+(\w+)\s+=\s+(.*)""".r
  private val stringRegex = """string\s+(\w+)\s+=\s+"(.*)"""".r
  private val changeIntRegex = """(\w+)\s+=(.*)""".r
  private val whileRegex = """while\s+\((\w+)\)""".r
  private val endWhileRegex = """end while""".r
  private val expressionRegex = """(\w+)\s*([+-])\s*(.+)""".r
  private val updateExpressionRegex = """(\w+)(\+\+|--)""".r
  private val printFunctionRegex = """print\((\w+)\)""".r
  private val readFunctionRegex = """read\((\w+)\)""".r
  private var program: MutableList[String] = MutableList()
  private var loop = 0

  /**
   * Translation javascript code to assembler
   *
   * @param js - input file (javascript)
   * @param as - output file (assembler)
   */
  def translate(js: String, as: String): Unit =
    val src = Source.fromFile(js)
    val lines = src.getLines().toList.map(s => s.trim)

    program += label("start", NULL())
    parse(lines)
    program += HLT()
    setVariables()

    Files.write(Paths.get(as), program.mkString("\n").getBytes(StandardCharsets.UTF_8))
    src.close

  @tailrec
  private def parse(lines: List[String]): Unit =
    if (lines.nonEmpty) {
      lines.head match
        case intRegex(name, expr) => intVariable(name, expr)
        case stringRegex(name, value) => stringVariable(name, value)
        case changeIntRegex(name, expr) => changeExpression(name, expr)
        case whileRegex(cond) => whileLoop(cond)
        case endWhileRegex() => whileEnd()
        case updateExpressionRegex(name, operand) => updateExpression(name, operand)
        case printFunctionRegex(arg) => printFunction(arg)
        case readFunctionRegex(arg) => readFunction(arg)
        case x if x.nonEmpty => throw new TranslationException(s"Can't translate expression \"${lines.head}\"")
        case _ => ()
      parse(lines.tail)
    }

  private def setVariables(): Unit =
    def char(c: Char): String = c.toInt.toHexString.toUpperCase

    val data: MutableList[String] = MutableList()
    nums.foreach(v => data += label(v._1, v._2.toHexString.toUpperCase))
    strings.foreach { s =>
      data += label(mkPtr(s._1), s"${s._1}")
      data += label(s._1, char(s._2.charAt(0)))
      s._2.substring(1).foreach(c => data.addOne(char(c)))
      data += "0"
    }
    program = data ++ program

  private def nameOrNum(s: String): Int =
    if (isNumber(s)) toNumber(s) else nums(s)

  private def evaluateExpression(expr: String): Int =
    @tailrec
    def helper(ex: String, mul: Int, acc: Int): Int =
      val n: String = ex.trim
      n match
        case expressionRegex(name, sign, tail) =>
          if (sign == "+") helper(tail, 1, acc + mul * nameOrNum(name))
          else helper(tail, -1, acc + mul * nameOrNum(name))
        case x => acc + nameOrNum(x)

    helper(expr, 1, 0)

  private def updateExpression(name: String, operand: String): Unit =
    program += LD(name, ABSOLUTE)
    if (operand == "++") program += INC() else program += DEC()
    program += ST(name, ABSOLUTE)

  private def changeExpression(name: String, express: String): Unit =
    def instr(name: String, plus: Boolean): String =
      val _type = if (isNumber(name)) DIRECT else ABSOLUTE
      if (plus) ADD(name, _type) else SUB(name, _type)

    def expression(expr: String): Unit =
      @tailrec
      def helper(ex: String, plus: Boolean): Unit =
        val n: String = ex.trim
        n match
          case expressionRegex(name, sign, tail) =>
            program += instr(name, plus)
            if (sign == "+") helper(tail, true)
            else helper(tail, false)
          case x => program += instr(x, plus)

      helper(expr, true)

    program += CLA()
    expression(express)
    program += ST(name, ABSOLUTE)

  private def intVariable(name: String, expr: String): Unit =
    nums(name) = evaluateExpression(expr)

  private def stringVariable(name: String, value: String): Unit =
    strings(name) = value

  private def whileLoop(cond: String): Unit =
    loop += 1
    loops.push(loop)
    val begin = lBegin(loop)
    val end = lEnd(loop)
    program += label(begin, LOOP(cond, ABSOLUTE))
    program += JUMP(end, ABSOLUTE)

  private def whileEnd(): Unit =
    val top = loops.pop()
    val begin = lBegin(top)
    val end = lEnd(top)
    program += JUMP(begin, ABSOLUTE)
    program += label(end, NULL())

  private def printFunction(arg: String): Unit =
    def printString(name: String): Unit =
      loop += 1
      val begin = lBegin(loop)
      val end = lEnd(loop)
      val ptr = mkPtr(name)
      program += label(begin, LD(ptr, RELATIVE))
      program += JZ(end, ABSOLUTE)
      program += OUT()
      program += LD(ptr, ABSOLUTE)
      program += INC()
      program += ST(ptr, ABSOLUTE)
      program += JUMP(begin, ABSOLUTE)
      program += label(end, NULL())

    if (nums.contains(arg)) program ++= List(LD(arg, ABSOLUTE), OUT())
    else if (strings.contains(arg)) printString(arg)

  private def readFunction(arg: String): Unit =
    program += IN()
    program += ST(arg, ABSOLUTE)

object Translator:

  private def lBegin(loop: Int) = s"loop$loop"

  private def lEnd(loop: Int) = s"end$loop"

  private def mkPtr(label: String): String = s"${label}ptr"

  private def label(label: String, body: String): String = s"$label: $body"

  private def isNumber(s: String): Boolean = s.forall(c => c.isDigit)

  private def toNumber(s: String): Int = s.toInt


