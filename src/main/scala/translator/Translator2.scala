package translator

import org.mozilla.javascript.{CompilerEnvirons, Parser}
import org.mozilla.javascript.ast.{Assignment, AstRoot, Block, ExpressionStatement, InfixExpression, Name, NumberLiteral, Scope, UpdateExpression, VariableDeclaration, VariableInitializer, WhileLoop}

import java.io.{File, FileReader}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import org.mozilla.javascript.Node
import org.mozilla.javascript.Token

import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import exception.TranslationException
import machine.AddressedCommand.*
import machine.AddressedCommand.Type.*
import machine.UnaddressedCommand.*
import translator.Translator2.label

import scala.annotation.tailrec

class Translator2:
  private val nums: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val prog: MutableList[String] = MutableList()
  private var loops = 0

  def progAdd(s: String*): Unit = prog.addAll(s.toList)

  def variable(v: VariableInitializer): Unit =
    val name = v.getTarget match
      case n: Name => n.getIdentifier
      case _ => throw new TranslationException("WTF")
    v.getInitializer match
      case n: NumberLiteral => nums(name) = n.getNumber.toInt
      case n: Name =>
        val id = n.getIdentifier
        if (nums.contains(id)) nums(name) = nums(id)
        else if (strings.contains(id)) strings(name) = strings(id)
        else throw new TranslationException("WTF")
      case _ => throw new TranslationException("WTF")

  def loop(l: WhileLoop): Unit =
    loops += 1
    val condLabel = l.getCondition.asInstanceOf[Name].getIdentifier
    val loopLabel = s"loop$loops"
    val endLabel = s"end$loops"
    progAdd (
      label(loopLabel, LOOP(condLabel, ADDR)),
      JUMP(endLabel, ADDR)
    )
    l.getBody.forEach(parseTree)
    progAdd (
      JUMP.mnemonic(loopLabel, ADDR),
      label(endLabel, NULL())
    )

  def expression(expr: InfixExpression): Unit =
    println()

  def expression(expr: UpdateExpression): Unit =
    val operandLabel = expr.getOperand.asInstanceOf[Name].getIdentifier
    progAdd(LD(operandLabel, ADDR))
    expr.getType match
      case Token.INC => prog.addOne(INC.mnemonic())
      case Token.DEC => prog.addOne(DEC.mnemonic())
    progAdd(ST(operandLabel, ADDR))

  def expression(expr: Assignment): Unit =
    val leftLabel = expr.getLeft.asInstanceOf[Name].getIdentifier
    expr.getRight match
      case n: (Name | NumberLiteral) =>
        val (_type, rightLabel) = n match
          case na: Name => (ADDR, na.getIdentifier)
          case nu: NumberLiteral => (DIRECT, nu.getValue)
        expr.getType match
          case Token.ASSIGN => progAdd(LD(rightLabel, _type))
          case Token.ASSIGN_ADD => progAdd(LD(leftLabel, _type), ADD(rightLabel, _type))
          case Token.ASSIGN_SUB => progAdd(LD(leftLabel, _type), SUB(rightLabel, _type))
          case _ => throw new TranslationException("WTF")
      case infixExpression: InfixExpression => expression(infixExpression)

    progAdd(ST(leftLabel, ADDR))

  def parseTree(n: Node): Unit =
    n match
      case a: AstRoot => a.forEach(parseTree)
      case w: WhileLoop => loop(w)
      case e: ExpressionStatement =>
        e.getExpression match
          case a: Assignment => expression(a)
          case u: UpdateExpression => expression(u)
          case _ => throw new TranslationException("WTF")
      case v: VariableDeclaration => ()
      case _ => throw new TranslationException("WTF")


  //set addresses for taken variables
  private def setVariables(): Unit =
    def char(c: Char): String = c.toInt.toHexString.toUpperCase
    nums.foreach (v => prog.addOne(s"${v._1}: ${v._2}"))
    strings.foreach { s =>
      prog.addOne(s"${s._1}: ${char(s._2.charAt(0))}")
      s._2.substring(1).foreach(c => prog.addOne(char(c)))
    }

  private def takeVariables(n: Node): Unit =
    n match
      case a: AstRoot => a.forEach(takeVariables)
      case v: VariableDeclaration => v.getVariables.forEach(variable)
      case w: WhileLoop => takeVariables(w.getBody)
      case s: Scope => s.forEach(takeVariables)
      case _ => ()


  def translate(input: String, output: String): Unit =
    val src = Source.fromFile(input)
    val lines = src.getLines().toList.mkString("\n")

    val compilerEnv = new CompilerEnvirons
    val errorReporter = compilerEnv.getErrorReporter
    val parser = new Parser(compilerEnv, errorReporter)

    val astRoot = parser.parse(lines, null, 1)

    takeVariables(astRoot)
    setVariables()
    parseTree(astRoot)

    prog.addOne(HLT.mnemonic())
    println(prog.mkString("\n"))

    src.close


object Translator2:

  def label(lab: String, body: String): String =
    s"$lab: $body"

