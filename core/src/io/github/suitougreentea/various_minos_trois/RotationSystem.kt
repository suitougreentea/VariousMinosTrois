package io.github.suitougreentea.various_minos_trois

interface RotationSystem {
    fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult
}

class RotationSystemDefault(): RotationSystem {
    override fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult {
        val newR = (r + dr + 4) % 4
        return if(GameUtil.hitTestMino(field, mino, x, y, newR)) RotationResult(false, Pos(0, 0), false) else RotationResult(true, Pos(0, 0), false)
    }
}
class RotationSystemStandard(): RotationSystem {
    // 0->R, R->X, X->L, L->0
    val superOffset3x3CW = arrayOf(
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos(-1, 1), Pos( 0,-2), Pos(-1,-2)),
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos( 1,-1), Pos( 0, 2), Pos( 1, 2)),
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos( 1, 1), Pos( 0,-2), Pos( 1,-2)),
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos(-1,-1), Pos( 0, 2), Pos(-1, 2))
    )
    val superOffset4x4CW = arrayOf(
            arrayOf(Pos( 0, 0), Pos(-2, 0), Pos( 1, 0), Pos(-2,-1), Pos( 1, 2)),
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos( 2, 0), Pos(-1, 2), Pos( 2,-1)),
            arrayOf(Pos( 0, 0), Pos( 2, 0), Pos(-1, 0), Pos( 2, 1), Pos(-1,-2)),
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos(-2, 0), Pos( 1,-2), Pos(-2, 1))
    )

    // 0->L, R->0, X->R, L->X
    val superOffset3x3CCW = arrayOf(
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos( 1, 1), Pos( 0,-2), Pos( 1,-2)),
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos( 1,-1), Pos( 0, 2), Pos( 1, 2)),
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos(-1, 1), Pos( 0,-2), Pos(-1,-2)),
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos(-1,-1), Pos( 0, 2), Pos(-1, 2))
    )
    val superOffset4x4CCW = arrayOf(
            arrayOf(Pos( 0, 0), Pos(-1, 0), Pos( 2, 0), Pos(-1, 2), Pos( 2,-1)),
            arrayOf(Pos( 0, 0), Pos( 2, 0), Pos(-1, 0), Pos( 2, 1), Pos(-1,-2)),
            arrayOf(Pos( 0, 0), Pos( 1, 0), Pos(-2, 0), Pos( 1,-2), Pos(-2, 1)),
            arrayOf(Pos( 0, 0), Pos(-2, 0), Pos( 1, 0), Pos(-2,-1), Pos( 1, 2))
    )

    val superOffsetNotImplemented = arrayOf(
            arrayOf(Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0)),
            arrayOf(Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0)),
            arrayOf(Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0)),
            arrayOf(Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0), Pos( 0, 0))
    )

    override fun attempt(field: Field, mino: Mino, x: Int, y: Int, r: Int, dr: Int): RotationResult {
        val newR = (r + dr + 4) % 4
        when(mino.minoId) {
            4 -> when(dr) {
                1 -> superOffset4x4CW
                -1 -> superOffset4x4CCW
                else -> superOffsetNotImplemented
            }
            5, 6, 7, 8, 9, 10 -> when(dr){
                1 -> superOffset3x3CW
                -1 -> superOffset3x3CCW
                else -> superOffsetNotImplemented
            }
            else -> superOffsetNotImplemented
        }[r].forEachIndexed { i, offset ->
            val (dx, dy) = offset
            if(!GameUtil.hitTestMino(field, mino, x + dx, y + dy, newR)) {
                return RotationResult(true, offset, i != 0)
            }
        }
        return RotationResult(false, Pos(0, 0), false)
    }
}

data class RotationResult(val success: Boolean, val offset: Pos, val kicked: Boolean)

