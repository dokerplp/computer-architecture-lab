package machine

private class Processor(val memory: Memory, val tg: TactGenerator) {
  def this() = this(new Memory, new TactGenerator)
  def this(stackSize: Int, wordSize: Int) = this(new Memory(stackSize, wordSize), new TactGenerator)


  val controlUnit = new ControlUnit(tg, memory)
}
