package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.Field
import io.github.suitougreentea.various_minos_trois.game.GameUtil
import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.Pos

interface RotationSystem {
  fun reset()
  fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult
}

class RotationSystemDefault(): RotationSystem {
  override fun reset() {}

  override fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult {
    val newR = (r + dr + 4) % 4
    return if(GameUtil.hitTestMino(field, mino, x, y, newR)) RotationResult(false, Pos(0, 0), false) else RotationResult(true, Pos(0, 0), false)
  }
}

data class RotationResult(val success: Boolean, val offset: Pos, val kicked: Boolean)
fun FailedRotationResult() = RotationResult(false, Pos(0, 0), false)

