package machine

import machine.Hardware
import machine.Register._

class AddressCommands(val hardware: Hardware):

  def add(addr: Int): Unit =
    val x = hardware(addr)
    hardware.ps(hardware(AC), x)
    (hardware +++ AC)(x)

  def sub(addr: Int): Unit =
    val x = hardware(addr)
    hardware.ps(hardware(AC), -x)
    (hardware --- AC)(x)

  def ld(addr: Int): Unit =
    hardware(AC) = hardware(addr)
    hardware.ps(hardware(AC), 0)

  def st(addr: Int): Unit =
    hardware(addr) = hardware(AC)

  def jump(addr: Int): Unit =
    hardware(IP) = hardware(addr)

  def call(addr: Int): Unit =
    val sp = hardware -- SP
    hardware(sp) = hardware(IP)
    hardware(IP) = addr



end AddressCommands
