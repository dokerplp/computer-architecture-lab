package machine

import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import machine.Memory.*

import scala.annotation.targetName
import scala.collection.mutable.{ArrayBuffer => MutableList}
import scala.collection.mutable.{Map => MutableMap}
import scala.math.*

class Memory:
  val dataRegisters: MutableMap[DataRegister, Int] = MutableMap() ++ DataRegister.values.map(r => (r, WORD_INIT)).toMap
  val addrRegisters: MutableMap[AddrRegister, Int] = MutableMap() ++ AddrRegister.values.map(r => (r, WORD_INIT)).toMap
  val reg = new Reg
  val mem = new Mem
  private val memory: MutableList[Int] = MutableList.tabulate(MEMORY_SIZE)(_ => WORD_INIT)
  private var _zero = true

  def zero: Boolean = _zero

  private def setFlag(): Unit = _zero = dataRegisters(AC) == 0

  class Reg:
    /**
     * Increase register by 1
     */
    @targetName("registerSubOne")
    def ++(r: DataRegister | AddrRegister): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) + 1)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) + 1)
      setFlag()

    /**
     * Decrease register by 1
     */
    @targetName("registerAddOne")
    def --(r: DataRegister | AddrRegister): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) - 1)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) - 1)
      setFlag()

    /**
     * Increase register by value
     */
    @targetName("registerAdd")
    def +++(r: DataRegister | AddrRegister)(value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) + value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) + value)
      setFlag()

    /**
     * Decrease register by value
     */
    @targetName("registerSub")
    def ---(r: DataRegister | AddrRegister)(value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(dataRegisters(d) - value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(addrRegisters(a) - value)
      setFlag()

    /**
     * Set register value
     */
    def update(r: DataRegister | AddrRegister, value: Int): Unit =
      r match
        case d: DataRegister => dataRegisters(d) = fixData(value)
        case a: AddrRegister => addrRegisters(a) = fixAddr(value)
      setFlag()

    /**
     * Get register value
     */
    def apply(r: DataRegister | AddrRegister): Int =
      r match
        case d: DataRegister => dataRegisters(d)
        case a: AddrRegister => addrRegisters(a)

  class Mem:
    /**
     * Set memory value
     */
    def update(addr: Int, value: Int): Unit =
      memory(addr) = value

    /**
     * Get memory value
     */
    def apply(addr: Int): Int =
      memory(addr)


object Memory:
  private val MEMORY_SIZE = 2048
  private val WORD_SIZE = 32
  private val WORD_INIT = 0
  val MAX_WORD: Int = (pow(2, WORD_SIZE - 1) - 1).toInt
  val MIN_WORD: Int = (-pow(2, WORD_SIZE - 1)).toInt
  val MAX_ADDR: Int = MEMORY_SIZE - 1

  /**
   * Mask for data registers
   */
  private def fixData(x: Int): Int = x

  /**
   * Mask for address registers
   */
  private def fixAddr(x: Int): Int =
    if (x > MAX_ADDR) x - MAX_ADDR - 1
    else if (x < 0) MAX_ADDR + x + 1
    else x

  enum DataRegister:
    case AC, DR, CR

  enum AddrRegister:
    case IP, AR




