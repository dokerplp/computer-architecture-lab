package machine

class User:
  private val processor = new Processor

  def load(instr: Int, addr: Int): Unit =
    processor.controlUnit.writeIP(addr)
    processor.controlUnit.input(instr)
  def load(instructions: List[Int], start: Int): Unit =
    processor.controlUnit.writeIP(start)
    instructions.foreach(processor.controlUnit.input)

//  def get(addr: Int): Int =
//    processor.controlUnit.writeIP(addr)
//    processor.controlUnit.output()
    
    

