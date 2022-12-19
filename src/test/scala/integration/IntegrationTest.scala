package integration

import machine.User
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.equal
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.should.Matchers.the
import org.scalatest.wordspec.AnyWordSpec
import translator.ISA
import translator.Translator

import scala.io.Source

class IntegrationTest extends AnyWordSpec  {
  
  "The program" should {
    "print \"hello world\"" in {
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

      res should equal ("hello world")
      src.close()
    }
    
    "print input string" in {
      val js = "./lang/cat.js"
      val as = "./lang/cat.as"
      val in = "./lang/cat.in"
      val out = "./lang/cat.out"
      val log = "./lang/cat.log"

      val translator = new Translator
      val isa = new ISA

      translator.translate(js, as)
      isa.translate(as = as, in = in, out = out, log = log, str = true)

      val srcOut = Source.fromFile(out)
      val srcIn = Source.fromFile(in)
      val input = srcOut.getLines().toList.mkString
      val output = srcIn.getLines().toList.mkString

      input should equal (output)
      
      srcOut.close()
      srcIn.close()
    }
    
    "solve Euler Problem 2" in {
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

      res should equal ("4613732")
    }
  }
  
}
