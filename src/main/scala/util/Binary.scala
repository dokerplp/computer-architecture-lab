package util

object Binary:

  /**
   * n-th bit
   */
  def bit(x: Int, n: Int): Int = (x >> n) & 1

  /**
   * 8-bit mask
   */
  def m8(x: Int): Int = x & 0x00FF

  /**
   * 11-bit mask
   */
  def m11(x: Int): Int = x & 0x07FF

  /**
   * 16-bit mask
   */
  def m16(x: Int): Int = x & 0xFFFF

  /**
   * Hex number to decimal
   */
  def hex(s: String): Int = Integer.parseInt(s, 16)

  /**
   * Decimal number to hex
   */
  def hex(x: Int): String = x.toHexString.toUpperCase
