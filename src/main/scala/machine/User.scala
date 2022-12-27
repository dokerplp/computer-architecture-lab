package machine

class User:
  val device = new Device
  val processor = new Processor(device)

  def load(instructions: List[Int], start: Int): Unit =
    device.IO = start
    processor.cu.writeIP()
    instructions.foreach { i =>
      device.IO = i
      processor.cu.input()
    }

  def run(ip: Int): Unit =
    processor.startProgram(ip)



