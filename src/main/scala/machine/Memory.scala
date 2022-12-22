package machine

import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import machine.Memory.*

import scala.annotation.targetName
import scala.collection.mutable.{ArrayBuffer => MutableList}
import scala.collection.mutable.{Map => MutableMap}
import scala.math.*

class Memory:
  val reg = new Reg
  val mem = new Mem
  private var _dataRegs: Map[DataRegister, Int] = DataRegister.values.map(r => (r, WORD_INIT)).toMap
  private var _addrRegs: Map[AddrRegister, Int] = AddrRegister.values.map(r => (r, WORD_INIT)).toMap
  private var memory: List[Int] = List.tabulate(MEMORY_SIZE)(_ => WORD_INIT)
  private var _zero = true

  def zero: Boolean = _zero

  def addrRegs: Map[AddrRegister, Int] = _addrRegs

  private def flag(): Unit =
    _zero = dataRegs(AC) == 0

  def dataRegs: Map[DataRegister, Int] = _dataRegs

  class Reg:

    private def change(d: DataRegister, value: Int): Unit =
      _dataRegs = _dataRegs updated(d, value)
      flag()

    private def change(a: AddrRegister, value: Int): Unit =
      _addrRegs = _addrRegs updated(a, value)
      flag()
    
    @targetName("regDec")
    def ++(r: DataRegister | AddrRegister): Unit =
      r match
        case d: DataRegister => change(d, fixData(_dataRegs(d) + 1))
        case a: AddrRegister => change(a, fixAddr(_addrRegs(a) + 1))

    @targetName("regInc")
    def --(r: DataRegister | AddrRegister): Unit =
      r match
        case d: DataRegister => change(d, fixData(_dataRegs(d) - 1))
        case a: AddrRegister => change(a, fixAddr(_addrRegs(a) - 1))

    def add(r: DataRegister | AddrRegister, value: Int): Unit =
      r match
        case d: DataRegister => change(d, fixData(_dataRegs(d) + value))
        case a: AddrRegister => change(a, fixAddr(_addrRegs(a) + value))

    def sub(r: DataRegister | AddrRegister, value: Int): Unit =
      r match
        case d: DataRegister => change(d, fixData(_dataRegs(d) - value))
        case a: AddrRegister => change(a, fixAddr(_addrRegs(a) - value))

    def update(r: DataRegister | AddrRegister, value: Int): Unit =
      r match
        case d: DataRegister => change(d, fixData(value))
        case a: AddrRegister => change(a, fixAddr(value))

    def apply(r: DataRegister | AddrRegister): Int =
      r match
        case d: DataRegister => _dataRegs(d)
        case a: AddrRegister => _addrRegs(a)

  class Mem:

    def update(addr: Int, value: Int): Unit =
      memory = memory updated(addr, value)

    def apply(addr: Int): Int =
      memory(addr)


object Memory:
  private val MEMORY_SIZE = 2048
  private val WORD_SIZE = 32
  private val WORD_INIT = 0
  val MAX_WORD: Int = (pow(2, WORD_SIZE - 1) - 1).toInt
  val MIN_WORD: Int = (-pow(2, WORD_SIZE - 1)).toInt
  val MAX_ADDR: Int = MEMORY_SIZE - 1

  private def fixData(x: Int): Int = x

  private def fixAddr(x: Int): Int =
    if (x > MAX_ADDR) x - MAX_ADDR - 1
    else if (x < 0) MAX_ADDR + x + 1
    else x

  enum DataRegister:
    case AC, DR, CR

  enum AddrRegister:
    case IP, AR




