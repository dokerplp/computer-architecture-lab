package machine

import machine.Hardware

class Instruction(val hardware: Hardware) {
  def add(addr: Int): Unit =
    val x = hardware(addr)
    hardware.+++(Register.AC)(x)

  def sub(addr: Int): Unit =
    val x = hardware(addr)
    hardware.---(Register.AC)(x)

  def ld(addr: Int): Unit =
    hardware(Register.AC) = hardware(addr)

  def st(addr: Int): Unit =
    hardware(addr) = hardware(Register.AC)

  def jump(addr: Int): Unit =
    hardware(Register.IP) = hardware(addr)

  def call(addr: Int): Unit =
    val sp = hardware -- Register.SP
    hardware(sp) = hardware(Register.IP)
    hardware(Register.IP) = hardware(addr)
}
