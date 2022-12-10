package machine

import exception.IllegalMemoryFormatException
import machine.Memory._

import scala.annotation.targetName
import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import scala.math._

class Memory:
  private val stack: MutableList[Int] = MutableList.tabulate(STACK_SIZE)(_ => WORD_INIT)
  private val registers: MutableMap[Register, Int] = MutableMap() ++ Register.values.map(r => (r, WORD_INIT)).toMap
  
  var buffer: List[Int] = List()

  val reg = new Reg
  val mem = new Mem

  class Reg:
    @targetName("registerSubOne")
    def ++(register: Register): Unit =
      registers(register) += 1

    @targetName("registerAddOne")
    def --(register: Register): Unit =
      registers(register) -= 1

    @targetName("registerAdd")
    def +++(register: Register)(value: Int): Unit =
      registers(register) += value

    @targetName("registerSub")
    def ---(register: Register)(value: Int): Unit =
      registers(register) -= value

    def update(register: Register, value: Int): Unit =
      registers(register) = value

    def apply(register: Register): Int =
      registers(register)

  class Mem:
    def update(addr: Int, value: Int): Unit =
      stack(addr) = value

    def apply(addr: Int): Int =
      stack(addr)
      
  

object Memory:

  private val STACK_SIZE = 2048
  private val WORD_SIZE = 16
  private val WORD_INIT = 0
  val MAX_WORD: Int = (pow(2, WORD_SIZE - 1) - 1).toInt
  val MIN_WORD: Int = (-pow(2, WORD_SIZE - 1)).toInt
  val MAX_ADDR: Int = STACK_SIZE - 1

  enum Register:
    case AC, ZR, DR, IP, CR, AR, IN




