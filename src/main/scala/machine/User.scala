package machine

class User:
  val device = new Device
  val processor = new Processor(device)

  /**
   * Load one instruction to processor
   *
   * @param instr - instruction code
   * @param addr  - address where instruction must be 
   */
  def load(instr: Int, addr: Int): Unit =
    device.IO = addr
    processor.controlUnit.writeIP()
    device.IO = instr
    processor.controlUnit.input()

  /**
   * Load lis of instructions to processor
   *
   * @param instructions - list of instructions
   * @param start        - address of the first instruction
   */
  def load(instructions: List[Int], start: Int): Unit =
    device.IO = start
    processor.controlUnit.writeIP()
    instructions.foreach { i =>
      device.IO = i
      processor.controlUnit.input()
    }

  /**
   * Model start
   *
   * @param ip - first address of the instruction
   */
  def run(ip: Int): Unit = processor.startProgram(ip)



