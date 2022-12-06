import machine.{Hardware, AddressCommands, Register}

/*
alg | acc | neum | hw | instr | struct | stream | port | prob2

alg - java-like language
acc - commands use accumulator
neum - data and commands in one stack
hw -
instr - each commands executes in one tact
struct - result of translation is json
stream - no interruptions
port -
*/
object Main {
  def main(args: Array[String]): Unit = {
    val hardware = new Hardware()
    val instruction = new AddressCommands(hardware)
  }
}