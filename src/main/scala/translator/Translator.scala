package translator

import exception.TranslationException
import machine.AddressedCommand.*
import machine.AddressedCommand.Type.*
import machine.UnaddressedCommand.*
import org.mozilla.javascript.ast.*
import org.mozilla.javascript.{CompilerEnvirons, Node, Parser, Token}
import translator.Translator.{label, nameOrNum, ptr}

import java.io.{File, FileReader, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer as MutableList, Map as MutableMap}
import scala.io.Source

class Translator:
  private val nums: MutableMap[String, Int] = MutableMap()
  private val strings: MutableMap[String, String] = MutableMap()
  private val program: MutableList[String] = MutableList()
  private var loops = 0
  private def lBegin = s"loop$loops"
  private def lEnd = s"end$loops"

  private def variable(v: VariableInitializer): Unit =
    val name = v.getTarget.asInstanceOf[Name].getIdentifier
    v.getInitializer match
      case n: NumberLiteral => nums(name) = n.getNumber.toInt
      case s: StringLiteral => strings(name) = s.getValue
      case n: Name =>
        val id = n.getIdentifier
        if (nums.contains(id)) nums(name) = nums(id)
        else if (strings.contains(id)) strings(name) = strings(id)
        else throw new TranslationException(s"Line ${v.getLineno}: unknown variable $id")
      case _ => throw new TranslationException(s"Line ${v.getLineno}: unknown variable type")

  private def loop(l: WhileLoop): Unit =
    loops += 1
    val cond = l.getCondition.asInstanceOf[Name].getIdentifier
    program += label(lBegin, LOOP(cond, ABSOLUTE))
    program += JUMP(lEnd, ABSOLUTE)
    l.getBody.forEach(parseTree)
    program += JUMP(lBegin, ABSOLUTE)
    program += label(lEnd, NULL())

  @tailrec
  private def expression(expr: InfixExpression): Unit =
    val (t, arg) = nameOrNum(expr.getRight)
    expr.getType match
      case Token.ADD => program += ADD(arg, t)
      case Token.SUB => program += SUB(arg, t)
      case _ => throw new TranslationException(s"Line ${expr.getLineno}: unknown infix expression")
    expr.getLeft match
      case i: InfixExpression => expression(i)
      case n: (Name | NumberLiteral) =>
        val (t2, arg2) = nameOrNum(n)
        program += ADD(arg2, t2)
      case _ => throw new TranslationException(s"Line ${expr.getLineno}: unknown expression")

  private def expression(expr: UpdateExpression): Unit =
    val op = expr.getOperand.asInstanceOf[Name].getIdentifier
    program += LD(op, ABSOLUTE)
    expr.getType match
      case Token.INC => program += INC()
      case Token.DEC => program += DEC()
    program += ST(op, ABSOLUTE)

  private def expression(expr: Assignment): Unit =
    val left = expr.getLeft.asInstanceOf[Name].getIdentifier
    expr.getRight match
      case n: (Name | NumberLiteral) =>
        val (t, right) = nameOrNum(n)
        expr.getType match
          case Token.ASSIGN => program += LD(right, t)
          case Token.ASSIGN_ADD => program ++= List(LD(left, t), ADD(right, t))
          case Token.ASSIGN_SUB => program ++= List(LD(left, t), SUB(right, t))
          case _ => throw new TranslationException(s"Line ${expr.getLineno}: unknown Assignment")
      case infixExpression: InfixExpression => expression(infixExpression)
    program += ST(left, ABSOLUTE)

  private def function(fun: FunctionCall): Unit =
    def printString(name: String): Unit =
      loops += 1
      val _ptr = ptr(name)
      program += label(lBegin, LD(_ptr, RELATIVE))
      program += JZ(lEnd, ABSOLUTE)
      program += OUT()
      program += LD(_ptr, ABSOLUTE)
      program += INC()
      program += ST(_ptr, ABSOLUTE)
      program += JUMP(lBegin, ABSOLUTE)
      program += label(lEnd, NULL())

    val name = fun.getTarget.asInstanceOf[Name].getIdentifier
    if (name != "print") throw new TranslationException(s"Line ${fun.getLineno}: invalid function name")
    val arg = fun.getArguments.get(0)
    arg match
      case n: Name =>
        val name = n.getIdentifier
        if (nums.contains(name)) program ++= List(LD(name, ABSOLUTE), OUT())
        else if (strings.contains(name)) printString(name)

  private def parseTree(n: Node): Unit =
    n match
      case a: AstRoot => a.forEach(parseTree)
      case w: WhileLoop => loop(w)
      case e: ExpressionStatement =>
        e.getExpression match
          case a: Assignment => expression(a)
          case u: UpdateExpression => expression(u)
          case f: FunctionCall => function(f)
          case _ => throw new TranslationException(s"Line ${e.getLineno}: invalid expression statement")
      case v: VariableDeclaration => ()
      case _ => throw new TranslationException(s"Line ${n.getLineno}: invalid node type")


  //set addresses for taken variables
  private def setVariables(): Unit =
    def char(c: Char): String = c.toInt.toHexString.toUpperCase

    nums.foreach(v => program += label(v._1, v._2.toString))
    strings.foreach { s =>
      program += label(ptr(s._1), s"${s._1}")
      program += label(s._1, char(s._2.charAt(0)))
      s._2.substring(1).foreach(c => program.addOne(char(c)))
      program += "0"
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

    program += label("start", NULL())
    parseTree(astRoot)
    program += HLT()

    Files.write(Paths.get(output), program.mkString("\n").getBytes(StandardCharsets.UTF_8))

    src.close


object Translator:
  def nameOrNum(n: Node): (Type, String) =
    n match
      case na: Name => (ABSOLUTE, na.getIdentifier)
      case nu: NumberLiteral => (DIRECT, nu.getValue)

  def ptr(lab: String): String = s"${lab}ptr"
  def label(lab: String, body: String): String = s"$lab: $body"

