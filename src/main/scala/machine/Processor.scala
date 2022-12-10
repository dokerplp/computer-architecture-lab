package machine

private class Processor {
  val memory: Memory = new Memory
  val tg: TactGenerator = new TactGenerator
  val controlUnit = new ControlUnit(tg, memory)

  def startProgram(ip: Int): Unit =
    controlUnit.writeIP(ip)
    controlUnit.commandFetch()
}
