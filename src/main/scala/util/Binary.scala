package util

object Binary:
  
  def bit(x: Int, n: Int) = (x >> n) & 1
  def m8(x: Int): Int = x & 0x00FF

  def m11(x: Int): Int = x & 0x07FF

  def m16(x: Int): Int = x & 0xFFFF
  def hex(s: String): Int = Integer.parseInt(s, 16)

  def hex(x: Int): String = x.toHexString.toUpperCase
