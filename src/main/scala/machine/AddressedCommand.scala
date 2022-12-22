package machine

import exception.TranslationException
import machine.AddressedCommand.Addressing
import machine.AddressedCommand.Addressing.*
import util.Binary.hex

enum AddressedCommand(val mnemonic: String, val binary: Character):
  case ADD extends AddressedCommand("ADD", '1')
  case SUB extends AddressedCommand("SUB", '2')
  case LOOP extends AddressedCommand("LOOP", '3')
  case LD extends AddressedCommand("LD", '4')
  case ST extends AddressedCommand("ST", '5')
  case JUMP extends AddressedCommand("JUMP", '6')
  case JZ extends AddressedCommand("JZ", '7')

  def apply(arg: String, addr: Addressing): String = mkMnemonic(arg, addr)
  
  def apply(arg: Int, addr: Addressing): Int =
    val instr = arg + mkBinary(addr)
    if (instr > Memory.MAX_WORD || instr < Memory.MIN_WORD)
      throw new TranslationException("Instruction doesn't match word format")
    else instr
  
  private def mkMnemonic(arg: String, addr: Addressing): String =
    addr match
      case ABSOLUTE => s"$mnemonic $$$arg"
      case DIRECT => s"$mnemonic #$arg"
      case RELATIVE => s"$mnemonic ($arg)"
      
  private def mkBinary(addr: Addressing): Int =
    addr match
      case ABSOLUTE => hex(s"${binary}000")
      case DIRECT => hex(s"${binary}800")
      case RELATIVE => hex(s"${binary}C00")

object AddressedCommand:

  def find(s: String): Option[AddressedCommand] =
    AddressedCommand.values.find(c => c.mnemonic == s)

  enum Addressing:
    case ABSOLUTE, DIRECT, RELATIVE
  

