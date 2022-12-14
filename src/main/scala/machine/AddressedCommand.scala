package machine

import machine.AddressedCommand.Type
import machine.AddressedCommand.Type._

enum AddressedCommand(val mnemonic: String, val binary: Character):
  case ADD extends AddressedCommand("ADD", '1')
  case SUB extends AddressedCommand("SUB", '2')
  case LOOP extends AddressedCommand("LOOP", '3')
  case LD extends AddressedCommand("LD", '4')
  case ST extends AddressedCommand("ST", '5')
  case JUMP extends AddressedCommand("JUMP", '6')
  case JZ extends AddressedCommand("JZ", '7')

  def apply(label: String, _type: AddressedCommand.Type): String =
    _type match
      case AddressedCommand.Type.ABSOLUTE => s"$mnemonic $$$label"
      case AddressedCommand.Type.DIRECT => s"$mnemonic #$label"
      case AddressedCommand.Type.RELATIVE => s"$mnemonic ($label)"

  def toBinary(_type: Type, arg: Int): Int =
    val left = _type match
      case ABSOLUTE => Integer.parseInt(s"${binary}000", 16)
      case DIRECT => Integer.parseInt(s"${binary}800", 16)
      case RELATIVE => Integer.parseInt(s"${binary}C00", 16)

    val res = left + arg
    if (res > Memory.MAX_WORD || res < Memory.MIN_WORD) throw new RuntimeException() else res    

object AddressedCommand:

  enum Type:
    case ABSOLUTE, DIRECT, RELATIVE
    
  def parse(s: String): Option[AddressedCommand] = 
    AddressedCommand.values.find(c => c.mnemonic == s)
  

