import machine.{Hardware, Instruction, Register}

object Main {
  def main(args: Array[String]): Unit = {
    val hardware = new Hardware()
    val instruction = new Instruction(hardware)
  }
}