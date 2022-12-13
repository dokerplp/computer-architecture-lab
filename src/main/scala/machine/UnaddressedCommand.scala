package machine

enum UnaddressedCommand(_mnemonic: String, _binary: String):
  case NULL extends UnaddressedCommand("NULL", "F000")
  case HLT extends UnaddressedCommand("HLT", "F100")
  case CLA extends UnaddressedCommand("CLA","F200")
  case INC extends UnaddressedCommand("INC","F300")
  case DEC extends UnaddressedCommand("DEC","F400")
  case OUT extends UnaddressedCommand("OUT","F500")
  
  def apply(): String = mnemonic()
  def mnemonic(): String = _mnemonic
  def binary(): String = _binary

//object UnaddressedCommand:
//  def mnemonicToBinary(mnemonic: String): String =
//    val opt = UnaddressedCommand.values.toList.find(com => com.mnemonic() == mnemonic)
//    if (opt.isDefined) opt.get.binary() else throw new RuntimeException()