package machine

import exception.EOFException

class Device:

  var IO: Int = 0
  var input: List[Int] = List.empty
  var output: List[Int] = List.empty
  private var ix = 0

  def read(): Unit =
    if (input.length <= ix) throw new EOFException("Buffer is empty")
    IO = input(ix)
    ix += 1

  def write(): Unit =
    output = output :+ IO

  def load(data: List[Int]): Unit =
    input = data
    ix = 0