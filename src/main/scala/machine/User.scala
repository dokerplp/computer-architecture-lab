package machine

class User:
  val device = new Device
  val processor = new Processor(device)

  def load(instr: Int, addr: Int): Unit =
    device.IO = addr
    processor.controlUnit.writeIP()
    processor.controlUnit.input()
  def load(instructions: List[Int], start: Int): Unit =
    device.IO = start
    processor.controlUnit.writeIP()
    instructions.foreach { i =>
      device.IO = i
      processor.controlUnit.input()
    }
  def run(ip: Int): Unit = processor.startProgram(ip)



