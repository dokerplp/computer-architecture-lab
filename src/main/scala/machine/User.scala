package machine

class User:
  private val _processor = new Processor
  def processor: Processor = _processor

  def load(instr: Int, addr: Int): Unit =
    processor.controlUnit.writeIP(addr)
    processor.controlUnit.input(instr)
  def load(instructions: List[Int], start: Int): Unit =
    processor.controlUnit.writeIP(start)
    instructions.foreach(processor.controlUnit.input)

  def run(ip: Int): Unit = _processor.startProgram(ip)

  def get(): List[Int] =
    processor.memory.buffer



