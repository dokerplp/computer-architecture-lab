package machine

import machine.Hardware._
import machine.Register._

import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import scala.Enumeration

class Hardware:
  val WORD_SIZE = 16
  val STACK_SIZE = 2048
  private val INITIAL_VALUE = 0

  private val stack: MutableList[Int] = MutableList.tabulate(STACK_SIZE)(_ => INITIAL_VALUE)
  private val registers: MutableMap[Register, Int] = MutableMap() ++ Register.values.map(reg => (reg, INITIAL_VALUE)).toMap
  registers(SP) = 2047

  def ++(register: Register): Int =
    registers(register) += 1
    registers(register)

  def --(register: Register): Int =
    registers(register) -= 1
    registers(register)

  def +++(register: Register)(value: Int): Int =
    registers(register) += value
    registers(register)

  def ---(register: Register)(value: Int): Int =
    registers(register) -= value
    registers(register)

  def update(register: Register, value: Int): Int =
    registers(register) = value
    registers(register)

  def update(addr: Int, value: Int): Int =
    stack(addr) = value
    stack(addr)

  def apply(register: Register): Int =
    registers(register)

  def apply(addr: Int): Int =
    stack(addr)

  def ps(ls: Int, rs: Int): Unit =
    var n, z, v, c = false
    val sum: Int = ls + rs
    val toWord: Int = 2 ^ (WORD_SIZE - 1) - 1
    n = bit(sum, WORD_SIZE - 1)
    z = (sum & toWord) == 0
    v = (ls > 0 && rs > 0 && n) || (ls < 0 && rs < 0 && !n)
    c = bit(sum, WORD_SIZE)
    registers(PS) = bit(n) * 1000 + bit(z) * 100 + bit(v) * 10 + bit(c)


end Hardware

object Hardware:
  def bit(word: Int, n: Int): Boolean = ((word >> (n - 1)) & 1) == 1

  def bit(bit: Boolean): Int = if (bit) 1 else 0


end Hardware





