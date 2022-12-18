package integration

import machine.User
import org.scalatest.funsuite.AnyFunSuite
import translator.{ISA, Translator}

import scala.io.Source

class IntegrationTest extends AnyFunSuite {

  test("hello world test") {
    val js = "./lang/helloWorld.js"
    val as = "./lang/helloWorld.as"
    val in = "./lang/helloWorld.in"
    val out = "./lang/helloWorld.out"
    val log = "./lang/helloWorld.log"

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log, str = true)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "hello world")
    src.close()
  }

  test("cat test") {
    val js = "./lang/cat.js"
    val as = "./lang/cat.as"
    val in = "./lang/cat.in"
    val out = "./lang/cat.out"
    val log = "./lang/cat.log"

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log, str = true)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "aboba")
  }

  test("Euler Problem 2 test") {
    val js = "./lang/euler2.js"
    val as = "./lang/euler2.as"
    val in = "./lang/euler2.in"
    val out = "./lang/euler2.out"
    val log = "./lang/euler2.log"

    val translator = new Translator
    val isa = new ISA

    translator.translate(js, as)
    isa.translate(as = as, in = in, out = out, log = log)

    val src = Source.fromFile(out)
    val res = src.getLines().toList.mkString

    assert(res == "4613732")
  }
}
