package machine

class TactGenerator:
  private var _tact: Int = 0

  def tact = _tact

  def tick(): Unit = _tact += 1

