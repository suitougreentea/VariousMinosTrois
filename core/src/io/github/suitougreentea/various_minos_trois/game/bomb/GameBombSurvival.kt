package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.Input

class GameBombSurvival(input: Input): GameBomb(input) {
  var level = 0
  var nextLevel = 100

  override fun newCycle() {
    level += 1
  }

  override fun newStateCounting() = StateCounting()
  override fun newStateAfterExplosion() = StateAfterExplosion()
  override fun newStateMakingBigBomb() = StateMakingBigBomb()

  open inner class StateCounting: GameBomb.StateCounting() {
    override fun leave() {
      super.leave()
      val lines = getLineState().filter { it == GameBomb.LineState.FILLED_WITHOUT_BOMB || it == GameBomb.LineState.FILLED_WITH_BOMB }.size
      val linesWithBomb = getLineState().filter { it == GameBomb.LineState.FILLED_WITH_BOMB }.size
      val basePoint = chain + lines - 1
      if(linesWithBomb == 0) level += basePoint
      else level += (basePoint * 1.5f).toInt()
    }
  }

  open inner class StateAfterExplosion: GameBomb.StateAfterExplosion() {
    override fun leave() {
      super.leave()
      level += (bombedBlocks * 0.07f).toInt()
    }
  }

  open inner class StateMakingBigBomb: GameBomb.StateMakingBigBomb() {
    override fun leave() {
      super.leave()
      level += bigBombList.size * 2
    }
  }
}