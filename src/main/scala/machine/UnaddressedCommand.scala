package machine

enum UnaddressedCommand(val mnemonic: String, val binary: String):
  case NULL extends UnaddressedCommand("NULL", "F000")
  case HLT extends UnaddressedCommand("HLT", "F100")
  case CLA extends UnaddressedCommand("CLA","F200")
  case INC extends UnaddressedCommand("INC","F300")
  case DEC extends UnaddressedCommand("DEC","F400")
  case IN extends UnaddressedCommand("IN","F500")
  case OUT extends UnaddressedCommand("OUT", "F600")

  def apply(): String = mnemonic

  def toBinary: Int = Integer.parseInt(binary, 16)
  

object UnaddressedCommand:
  def parse(s: String): Option[UnaddressedCommand] =
    UnaddressedCommand.values.find(c => c.mnemonic == s)