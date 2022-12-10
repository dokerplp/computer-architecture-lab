package machine

import exception.IllegalMemoryFormatException
import machine.Memory._

import scala.annotation.targetName
import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import scala.math._

class Memory(val stackSize: Int = DEFAULT_STACK_SIZE, wordSize: Int = DEFAULT_WORD_SIZE):
  if (stackSize < MIN_STACK_SIZE) throw new IllegalMemoryFormatException("Stack size must be greater or equals to 100")
  if (wordSize < MIN_WORD_BIT_DEPTH || wordSize > MAX_WORD_BIT_DEPTH) throw new IllegalMemoryFormatException("Word size must be between 8 and 32")

  val maxWord: Int = (pow(2, wordSize - 1) - 1).toInt
  val minWord: Int = (-pow(2, wordSize - 1)).toInt
  val maxAddr: Int = stackSize - 1

  private val stack: MutableList[Int] = MutableList.tabulate(stackSize)(_ => WORD_INIT)
  private val registers: MutableMap[Register, Int] = MutableMap() ++ Register.values.map(r => (r, WORD_INIT)).toMap
  
  val buffer: MutableList[Int] = MutableList()

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

  private val DEFAULT_STACK_SIZE = 2048
  private val DEFAULT_WORD_SIZE = 16
  private val MIN_STACK_SIZE = 100
  private val MIN_WORD_BIT_DEPTH = 8
  private val MAX_WORD_BIT_DEPTH = 32
  private val WORD_INIT = 0

  enum Register:
    case AC, ZR, DR, IP, CR, AR, IN




