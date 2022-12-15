package integration

import machine.User
import org.scalatest.funsuite.AnyFunSuite
import translator.{ISA, Translator}

import scala.io.Source

class IntegrationTest extends AnyFunSuite {

  val translator = new Translator

  test("hello world test") {
    val js = "./lang/helloWorld.js"
    val as = "./lang/helloWorld.as"
    val out = "./lang/helloWorld.txt"
    val log = "./lang/helloWorld.log"

    val user = new User
    val isa = new ISA(user)

    translator.translate(js, as)
    isa.translate(as, out, log, true)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "hello world")
  }

  test("cat test") {
    val js = "./lang/cat.js"
    val as = "./lang/cat.as"
    val out = "./lang/cat.txt"
    val log = "./lang/cat.log"

    val user = new User
    val msg = "aboba"
    user.device.input = msg.chars().toArray.toList :+ 0
    val isa = new ISA(user)

    translator.translate(js, as)
    isa.translate(as, out, log, true)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "aboba")
  }

  test("Euler Problem 2 test") {
    val js = "./lang/euler2.js"
    val as = "./lang/euler2.as"
    val out = "./lang/euler2.txt"
    val log = "./lang/euler2.log"

    val user = new User
    val isa = new ISA(user)

    translator.translate(js, as)
    isa.translate(as, out, log)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "List(4613732)")
  }
}
