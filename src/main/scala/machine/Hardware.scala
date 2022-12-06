package machine

import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap

import scala.Enumeration

class Hardware:
  private val STACK_SIZE = 2048
  private val INITIAL_VALUE = 0

  private val stack: MutableList[Int] = MutableList.tabulate(STACK_SIZE)(_ => INITIAL_VALUE)
  private val registers: MutableMap[Register, Int] = MutableMap() ++ Register.values.map(reg => (reg, INITIAL_VALUE)).toMap
  registers(Register.SP) = 2047

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


end Hardware




