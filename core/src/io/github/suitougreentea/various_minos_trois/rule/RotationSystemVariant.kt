package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.Field
import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.Pos
import io.github.suitougreentea.various_minos_trois.game.GameUtil

class RotationSystemVariant(val classic: Boolean): RotationSystem {
  val offsetCW2I4I = arrayOf(Pos(0, 0), Pos(0, 1), Pos(1, -1), Pos(-1, 0))
  val offsetCCW2I4I = arrayOf(Pos(1, 0), Pos(0, 0), Pos(0, -1), Pos(-1, 1))
  val offsetCW4S = arrayOf(Pos(-1, 1), Pos(1, 0), Pos(0, 0), Pos(0, -1))
  val offsetCCW4S = arrayOf(Pos(0, 1), Pos(1, -1), Pos(-1, 0), Pos(0, 0))
  val offsetCW4Z = arrayOf(Pos(0, 1), Pos(0, 0), Pos(1, 0), Pos(-1, -1))
  val offsetCCW4Z = arrayOf(Pos(1, 1), Pos(0, -1), Pos(0, 0), Pos(-1, 0))
  val offsetCW32 = arrayOf(Pos(0, 1), Pos(0, 0), Pos(0, 0), Pos(0, -1))
  val offsetCCW32 = arrayOf(Pos(0, 1), Pos(0, -1), Pos(0, 0), Pos(0, 0))
  val offsetCW42 = arrayOf(Pos(0, 1), Pos(0, -1), Pos(0, 1), Pos(0, -1))
  val offsetCCW42 = arrayOf(Pos(0, 1), Pos(0, -1), Pos(0, 1), Pos(0, -1))

  val offsetNone = arrayOf(Pos(0, 0), Pos(0, 0), Pos(0, 0), Pos(0, 0))

  var specialKicked = false

  override fun reset() {
    specialKicked = false
  }

  override fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult {
    val newR = (r + dr + 4) % 4
    val (dx, dy) = when(mino.minoId) {
      1, 4 -> when(dr) {
        1 -> offsetCW2I4I
        -1 -> offsetCCW2I4I
        else -> offsetNone
      }
      8 -> when(dr) {
        1 -> offsetCW4S
        -1 -> offsetCCW4S
        else -> offsetNone
      }
      10 -> when(dr) {
        1 -> offsetCW4Z
        -1 -> offsetCCW4Z
        else -> offsetNone
      }
      5, 6, 9, 18, 19, 21 -> when(dr) {
        1 -> offsetCW32
        -1 -> offsetCCW32
        else -> offsetNone
      }
      14, 15, 16, 17, 25, 26 -> when(dr) {
        1 -> offsetCW42
        -1 -> offsetCCW42
        else -> offsetNone
      }
      0, 2, 3, 7, 13, 11, 12, 20, 22, 23, 24, 27, 28 -> offsetNone
      else -> offsetNone
    }[r]

    when(mino.minoId) {
      1, 2, 4, 13 -> {
        if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy, newR)) {
          return RotationResult(true, Pos(dx, dy), false)
        }
        if(!classic) {
          if((r == 1 || r == 3) && (dr == -1 || dr == 1)) {
            // Wall kick
            if(!GameUtil.hitTestMino(field, mino, x + dx + 1, y + dy, newR)) {
              return RotationResult(true, Pos(dx + 1, dy), true)
            }
            if(mino.minoId == 1) return FailedRotationResult()
            if(!GameUtil.hitTestMino(field, mino, x + dx - 1, y + dy, newR)) {
              return RotationResult(true, Pos(dx - 1, dy), true)
            }
            if(mino.minoId == 2) return FailedRotationResult()
            if(!GameUtil.hitTestMino(field, mino, x + dx + 2, y + dy, newR)) {
              return RotationResult(true, Pos(dx + 2, dy), true)
            }
            if(mino.minoId == 4) return FailedRotationResult()
            if(!GameUtil.hitTestMino(field, mino, x + dx - 2, y + dy, newR)) {
              return RotationResult(true, Pos(dx - 2, dy), true)
            }
          }
          if((r == 0 || r == 2) && (dr == -1 || dr == 1) && !specialKicked) {
            // Floor kick
            if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy - 1, r)) {
              // I doesn't kick when in mid-air
              return FailedRotationResult()
            }
            if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy + 1, newR)) {
              specialKicked = true
              return RotationResult(true, Pos(dx, dy + 1), true)
            }
            if(mino.minoId == 1 || mino.minoId == 2) return FailedRotationResult()
            if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy + 2, newR)) {
              specialKicked = true
              return RotationResult(true, Pos(dx, dy + 2), true)
            }
          }
        }
      }
      else -> {
        if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy, newR)) {
          return RotationResult(true, Pos(dx, dy), false)
        }

        if((mino.minoId == 5 || mino.minoId == 6 || mino.minoId == 9) && (r == 0 || r == 2)) {
          val oy = if(r == 2) -1 else 0
          val centerUp = field.get(Pos(x + 1, y + 3 + oy))
          val centerCenter = field.get(Pos(x + 1, y + 2 + oy))
          val centerDown = field.get(Pos(x + 1, y + 1 + oy))
          val leftUp = field.get(Pos(x, y + 3 + oy))
          val rightUp = field.get(Pos(x + 2, y + 3 + oy))
          if(centerUp != null) return FailedRotationResult()
          if(centerCenter != null && !((mino.minoId == 5 && r == 0 && rightUp != null) || (mino.minoId == 6 && r == 0 && leftUp != null))) return FailedRotationResult()
          if(centerDown != null && !((mino.minoId == 5 && r == 2 && rightUp != null) || (mino.minoId == 6 && r == 2 && leftUp != null))) return FailedRotationResult()
        }

        if(!GameUtil.hitTestMino(field, mino, x + dx + 1, y + dy, newR)) {
          return RotationResult(true, Pos(dx + 1, dy), true)
        }
        if(!GameUtil.hitTestMino(field, mino, x + dx - 1, y + dy, newR)) {
          return RotationResult(true, Pos(dx - 1, dy), true)
        }
        if(mino.minoId == 9 && newR == 0 && !specialKicked) {
          // T Floor kick
          if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy + 1, newR)) {
            specialKicked = true
            return RotationResult(true, Pos(dx, dy + 1), true)
          }
        }
      }
    }

    return FailedRotationResult()
  }
}