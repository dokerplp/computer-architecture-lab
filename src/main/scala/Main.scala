import machine.User
import translator.{ISA, Translator}

import scala.io.Source

object Main:

  val translator = new Translator
  val isa = new ISA
  def alg(name: String, j: String, a: String, o: String, l: String, user: User, str: Boolean = false): Unit =
    println(name)
    translator.translate(j, a)
    isa.translate(a, o, l, user, str)

    val src = Source.fromFile(o)
    val res = src.getLines().toList.mkString

    println(s"Result: \"$res\"\n\n")
  def main(args: Array[String]): Unit =

    val js = ("./lang/euler2.js", "./lang/helloWorld.js", "./lang/cat.js")
    val as = ("./lang/euler2.as", "./lang/helloWorld.as", "./lang/cat.as")
    val out = ("./lang/euler2.txt", "./lang/helloWorld.txt", "./lang/cat.txt")
    val log = ("./lang/euler2.log", "./lang/helloWorld.log", "./lang/cat.log")

    alg("Euler Problem 2", js._1, as._1, out._1, log._1, new User)
    alg("Hello world", js._2, as._2, out._2, log._2, new User, true)
    val user = new User
    val message = "aboba"
    user.device.input = message.chars().toArray.toList :+ 0
    alg("Cat", js._3, as._3, out._3, log._3, user,true)