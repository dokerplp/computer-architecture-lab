package translator

import exception.TranslationException

import scala.annotation.tailrec
import scala.io.Source

import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap

class Translator:
  private val ints: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val addrs: MutableMap[String, Int] = MutableMap()
  private val prog: MutableList[Int] = MutableList()
  private var addr: Int = 0

  private val intRegex = """int\s+([a-zA-Z]+)\s*=\s*(.+)""".r
  private val intValueRegex = """(-?[1-9]\d*)""".r
  private val varValueRegex = """([a-zA-Z]+)""".r
  private val expressionRegex = """(\w+)\s*([+-])\s*(.+)""".r
  private val stringRegex = """string\s+([a-zA-Z]+)\s*=\s*\"([a-zA-Z]+)\"""".r

  final def expressionParser(expression: String, num: Int): Int =
    @tailrec
    def helper(expression: String, acc: Int, sign: Int): Int =
      expression match
        case intValueRegex(value) =>
          val numOpt = value.toIntOption
          if (numOpt.isEmpty) throw new TranslationException(s"Line $num: can't cast variable \"$value\" to integer type.")
          acc + sign * numOpt.get
        case varValueRegex(value) =>
          if (!ints.contains(value)) throw new TranslationException(s"Line $num: can't find variable \"$value\".")
          acc + sign * ints(value)
        case expressionRegex(name, op, expr) =>
          var num = 0
          val numOpt = name.toIntOption
          if (numOpt.isDefined) num = numOpt.get
          else if (ints.contains(name)) num = ints(name)
          else throw new TranslationException(s"Line $num: can't cast variable \"$name\" to integer type.")
          val newSign = if (op == "-") -1 else 1
          helper(expr, acc + sign * num, newSign)
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
          if (!ints.contains(name)) ints(name) = expressionParser(expr, num)
          else throw new TranslationException(s"Line $num: double declaration of variable $name")
        case _ => ()
      translationStage1(lines.tail, num + 1)

  def translationStage2(): Unit =
    for (v <- ints) {
      addrs(v._1) = addr
      prog.addOne(v._2)
      addr += 1
    }
    for (v <- strings) {
      addrs(v._1) = addr
      for (c <- v._2) {
        prog.addOne(c.toInt)
        addr += 1
      }
      prog.addOne(0)
      addr += 1
    }

  //def translationStage3(): Unit =



  def translate(input: String, output: String): Unit = {
    val src = Source.fromFile(input)
    val lines = src.getLines().toList
    translationStage1(lines, 1)
    translationStage2()
    //translationStage3()
    src.close
  }

