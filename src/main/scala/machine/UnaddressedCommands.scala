package machine

import machine.Register._
class UnaddressedCommands(val hardware: Hardware):
  def cla(): Unit =
    hardware(AC) = 0
    hardware.ps(0, 0)

  def hlt(): Unit =
    throw new HLTException("The program was stopped due to the command HLT")

  def inc(): Unit =
    hardware.ps(hardware(AC), 1)
    hardware ++ AC

  def dec(): Unit =
    hardware.ps(hardware(AC), -1)
    hardware -- AC

  def pop(): Unit =
    val sp = hardware(SP)
    val addr = hardware(sp)
    hardware(AC) = addr
    hardware ++ SP
    hardware.ps(hardware(AC), 0)

  def push(): Unit =
    val sp = hardware -- SP
    hardware(sp) = hardware(AC)

end UnaddressedCommands

