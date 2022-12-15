package machine

class TactGenerator:
  private var _tact: Int = 0

  def tact: Int = _tact

  def tick(): Unit = _tact += 1
  
  def clean(): Unit = _tact = 0

