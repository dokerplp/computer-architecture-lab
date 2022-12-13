package machine

enum AddressedCommand(_mnemonic: String, _binary: String):
  case ADD extends AddressedCommand("ADD", "1")
  case SUB extends AddressedCommand("SUB", "2")
  case LOOP extends AddressedCommand("LOOP", "3")
  case LD extends AddressedCommand("LD", "4")
  case ST extends AddressedCommand("ST", "5")
  case JUMP extends AddressedCommand("JUMP", "6")
  case JZ extends AddressedCommand("JZ", "7")
  
  def apply(label: String, _type: AddressedCommand.Type): String = mnemonic(label, _type)
  def mnemonic(label: String, _type: AddressedCommand.Type): String =
    _type match
      case AddressedCommand.Type.ADDR => s"$_mnemonic $$$label"
      case AddressedCommand.Type.DIRECT => s"$_mnemonic #$label"

object AddressedCommand:
  
  enum Type:
    case ADDR, DIRECT

//  private val ADDR = """([A-Z]+)\s+\$(\w+)""".r
//  private val DIRECT = """([A-Z]+)\s+#(\w+)""".r
//  def mnemonicToBinary(mnemonic: String): String =
//    mnemonic match
//      case ADDR(mnemonic, addr) =>
//        val num = Integer.parseInt(addr, 16)
//        val opt = AddressedCommand.values.toList.find(com => com.mnemonic() == mnemonic)
//        if (opt.isDefined) opt.get.binaryAddr(num) else throw new RuntimeException()
//      case DIRECT(mnemonic, value) =>
//        val num = Integer.parseInt(value, 16)
//        val opt = AddressedCommand.values.toList.find(com => com.mnemonic() == mnemonic)
//        if (opt.isDefined) opt.get.binaryDirect(num) else throw new RuntimeException()
//      case _ => throw new RuntimeException()