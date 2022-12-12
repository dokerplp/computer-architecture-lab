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
import machine.ControlUnit.AddressCommands.*
import machine.ControlUnit.UnaddressedCommands.*

class Translator2:
  private val nums: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val addrs: MutableMap[String, Int] = MutableMap()
  private val prog: MutableList[Int] = MutableList()

  def variable(v: VariableInitializer): Unit =
    val target = v.getTarget
    val initializer = v.getInitializer
    val name = target.getType match
      case Token.NAME => target.asInstanceOf[Name].getIdentifier
      case _ => throw new TranslationException("WTF")
    initializer.getType match
      case Token.NUMBER => val v = initializer.asInstanceOf[NumberLiteral].getNumber.toInt; nums(name) = v
      case Token.NAME =>
        val n = initializer.asInstanceOf[Name].getIdentifier
        if (nums.contains(n)) nums(name) = nums(n)
        else if (strings.contains(n)) strings(name) = strings(n)
        else throw new TranslationException("WTF")
      case _ => throw new TranslationException("WTF")

  def loop(l: WhileLoop): Unit =
    val cond = l.getCondition.asInstanceOf[Name].getIdentifier
    if (!nums.contains(cond)) throw new TranslationException("WTF")
    val start = prog.length
    prog.addOne(LOOP(addrs(cond)))
    prog.addOne(0)
    l.getBody.forEach(parseTree)
    prog.addOne(JUMP(start))
    prog(start + 1) = prog.length

  def expression(expr: InfixExpression): Unit =
    println()
  def expression(expr: UpdateExpression): Unit =
    val operand = expr.getOperand.asInstanceOf[Name].getIdentifier
    if (!nums.contains(operand)) throw new TranslationException("WTF")
    val opAddr = addrs(operand)
    expr.getType match
      case Token.INC => prog.addAll(List(LD(opAddr), INC(), ST(opAddr)))
      case Token.DEC => prog.addAll(List(LD(opAddr), DEC(), ST(opAddr)))
  def expression(expr: Assignment): Unit =
    val left = expr.getLeft.asInstanceOf[Name].getIdentifier
    if (!nums.contains(left)) throw new TranslationException("WTF")
    val leftAddr = addrs(left)

    expr.getRight match
      case name: Name =>
        val right = name.getIdentifier
        if (!nums.contains(right)) throw new TranslationException("WTF")
        val rightAddr = addrs(right)
        expr.getType match
          case Token.ASSIGN => prog.addOne(LD(rightAddr))
          case Token.ASSIGN_ADD => prog.addAll(List(LD(leftAddr), ADD(rightAddr)))
          case Token.ASSIGN_SUB => prog.addAll(List(LD(leftAddr), SUB(rightAddr)))
          case _ => throw new TranslationException("WTF")
      case number: NumberLiteral =>
        val value = number.getNumber.toInt
        expr.getType match
          case Token.ASSIGN => prog.addOne(LD.direct(value))
          case Token.ASSIGN_ADD => prog.addAll(List(LD(leftAddr), ADD.direct(value)))
          case Token.ASSIGN_SUB => prog.addAll(List(LD(leftAddr), SUB.direct(value)))
          case _ => throw new TranslationException("WTF")
      case infixExpression: InfixExpression => expression(infixExpression)

    prog.addOne(ST(leftAddr))

  def parseTree(n: Node): Unit =
    n.getType match
      case Token.SCRIPT => n.forEach(parseTree)
      case Token.WHILE => val l = n.asInstanceOf[WhileLoop]; loop(l)
      case Token.EXPR_RESULT =>
        n.asInstanceOf[ExpressionStatement].getExpression match
          case assignment: Assignment => expression(assignment)
          case updateExpression: UpdateExpression => expression(updateExpression)
          case _ => throw new TranslationException("WTF")
      case Token.VAR => ()
      case _ => throw new TranslationException("WTF")


  //set addresses for taken variables
  def setAddresses(): Unit =
    for (v <- nums) {
      addrs(v._1) = prog.length
      prog.addOne(v._2)
    }
    for (v <- strings) {
      addrs(v._1) = prog.length
      for (c <- v._2) prog.addOne(c.toInt)
      prog.addOne(0)
    }

  //take all variables
  def takeVariables(n: Node): Unit =
    n.getType match
      case Token.SCRIPT => n.forEach(takeVariables)
      case Token.VAR => n.asInstanceOf[VariableDeclaration].getVariables.forEach(variable)
      case Token.WHILE => takeVariables(n.asInstanceOf[WhileLoop].getBody)
      case Token.BLOCK => n.asInstanceOf[Scope].forEach(takeVariables)
      case _ => ()


  def translate(input: String, output: String): Unit =
    val src = Source.fromFile(input)
    val lines = src.getLines().toList.mkString("\n")

    val compilerEnv = new CompilerEnvirons
    val errorReporter = compilerEnv.getErrorReporter
    val parser = new Parser(compilerEnv, errorReporter)

    val astRoot = parser.parse(lines, null, 1)

    takeVariables(astRoot)
    setAddresses()
    parseTree(astRoot)

    src.close

