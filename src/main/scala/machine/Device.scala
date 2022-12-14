package machine
class Device:

  var IO: Int = 0
  private var ix = 0

  var input: List[Int] = List()
  var output: List[Int] = List()

  def read(): Unit =
    IO = input(ix)
    ix += 1

  def write(): Unit =
    output = output :+ IO
