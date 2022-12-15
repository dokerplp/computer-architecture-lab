package machine

import util.Binary.hex
import exception.TranslationException
import machine.AddressedCommand.Type
import machine.AddressedCommand.Type.*

enum AddressedCommand(val mnemonic: String, val binary: Character):
  case ADD extends AddressedCommand("ADD", '1')
  case SUB extends AddressedCommand("SUB", '2')
  case LOOP extends AddressedCommand("LOOP", '3')
  case LD extends AddressedCommand("LD", '4')
  case ST extends AddressedCommand("ST", '5')
  case JUMP extends AddressedCommand("JUMP", '6')
  case JZ extends AddressedCommand("JZ", '7')

  /**
   * Mnemonic constructor
   * @param label - argument
   * @param _type - addressing type
   * @return mnemonic
   */
  def apply(label: String, _type: Type): String =
    _type match
      case ABSOLUTE => s"$mnemonic $$$label"
      case DIRECT => s"$mnemonic #$label"
      case RELATIVE => s"$mnemonic ($label)"

  /**
   * Binary format of command
   * @param _type - addressing type
   * @param arg - argument for checking word size
   * @return command in binary format
   */
  def toBinary(_type: Type, arg: Int): Int =
    val com = _type match
      case ABSOLUTE => hex(s"${binary}000")
      case DIRECT => hex(s"${binary}800")
      case RELATIVE => hex(s"${binary}C00")
    val instr = com + arg
    if (instr > Memory.MAX_WORD || instr < Memory.MIN_WORD) throw new TranslationException("Instruction doesn't match word format") else instr

object AddressedCommand:

  /**
   * Finds element of enum by mnemonic
   */
  def parse(s: String): Option[AddressedCommand] =
    AddressedCommand.values.find(c => c.mnemonic == s)

  enum Type:
    case ABSOLUTE, DIRECT, RELATIVE
  

