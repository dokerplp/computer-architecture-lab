package machine

import machine.Memory._

import scala.annotation.targetName
import scala.collection.mutable.ArrayBuffer as MutableList
import scala.collection.mutable.Map as MutableMap
import scala.math._

import Memory.DataRegister._
import Memory.AddrRegister._

class Memory:
  private val memory: MutableList[Int] = MutableList.tabulate(MEMORY_SIZE)(_ => WORD_INIT)

  val dataRegisters: MutableMap[DataRegister, Int] = MutableMap() ++ DataRegister.values.map(r => (r, WORD_INIT)).toMap
  val addrRegisters: MutableMap[AddrRegister, Int] = MutableMap() ++ AddrRegister.values.map(r => (r, WORD_INIT)).toMap

  private var _zero = false
  def zero = _zero
  def setFlag(): Unit = _zero = dataRegisters(AC) == 0


  val reg = new Reg
  val mem = new Mem

  class Reg:
    @targetName("registerSubOne")
    def ++(r: (DataRegister | AddrRegister)): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) + 1)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) + 1)
      setFlag()

    @targetName("registerAddOne")
    def --(r: (DataRegister | AddrRegister)): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) - 1)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) - 1)
      setFlag()

    @targetName("registerAdd")
    def +++(r: (DataRegister | AddrRegister))(value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) + value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) + value)
      setFlag()

    @targetName("registerSub")
    def ---(r: (DataRegister | AddrRegister))(value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) - value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) - value)
      setFlag()

    def update(r: (DataRegister | AddrRegister), value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(value)
      setFlag()

    def apply(r: (DataRegister | AddrRegister)): Int =
      r match
        case d: DataRegister => dataRegisters(d)
        case a: AddrRegister => addrRegisters(a)

  class Mem:
    def update(addr: Int, value: Int): Unit =
      memory(addr) = value

    def apply(addr: Int): Int =
      memory(addr)
      
  

object Memory:

  private val MEMORY_SIZE = 2048
  private val WORD_SIZE = 16
  private val WORD_INIT = 0
  val MAX_WORD: Int = (pow(2, WORD_SIZE - 1) - 1).toInt
  val MIN_WORD: Int = (-pow(2, WORD_SIZE - 1)).toInt
  val MAX_ADDR: Int = MEMORY_SIZE - 1

  enum DataRegister:
    case AC, DR, CR
  enum AddrRegister:
    case IP, AR

  private def fixData(x: Int): Int =
    if (x > MAX_WORD) MIN_WORD + x - MAX_WORD - 1
    else if (x < MIN_WORD) MAX_WORD + x - MIN_WORD + 1
    else x

  private def fixAddr(x: Int): Int =
    if (x > MAX_ADDR) x - MAX_ADDR - 1
    else if (x < 0) MAX_ADDR + x + 1
    else x




