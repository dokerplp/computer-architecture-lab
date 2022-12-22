package machine

import machine.Memory.AddrRegister.*
import machine.Memory.DataRegister.*
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

class MemoryTest extends AnyWordSpec {

  "Registers" should {
    "increment on ++" in {
      val m = new Memory
      m.reg ++ IP
      m.reg ++ AC

      m.reg(IP) should equal (1)
      m.reg(AC) should equal (1)
    }

    "decrement on --" in {
      val m = new Memory
      m.reg -- IP
      m.reg -- AC

      m.reg(IP) should equal(Memory.MAX_ADDR)
      m.reg(AC) should equal(-1)
    }

    "increase on add" in {
      val m = new Memory
      m.reg.add(IP, 5)
      m.reg.add(AC, 5)

      m.reg(IP) should equal(5)
      m.reg(AC) should equal(5)
    }

    "decrease on sub" in {
      val m = new Memory
      m.reg.sub(IP, 5)
      m.reg.sub(AC, 5)

      m.reg(IP) should equal(Memory.MAX_ADDR - 4)
      m.reg(AC) should equal(-5)
    }

    "set new value on update and get value on apply" in {
      val m = new Memory
      m.reg(IP) = 42
      m.reg(AC) = 42

      m.reg(IP) should equal(42)
      m.reg(AC) should equal(42)
    }

  }

  "Memory" should {
    "set new value on update and get value on apply" in {
      val m = new Memory
      m.mem(42) = 228

      m.mem(42) should equal (228)
    }
  }

}
