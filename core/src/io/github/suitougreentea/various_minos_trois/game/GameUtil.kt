package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.Field
import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.Pos

object GameUtil {
    fun hitTestMino(field: Field, mino: Mino, x: Int, y: Int, r: Int): Boolean {
        val blocks = mino.getRotatedBlocks(r)
        return blocks.any { e ->
            val dx = x + e.first.x
            val dy = y + e.first.y
            (dx < 0 || field.width <= dx || dy < 0 || field.height <= dy || field.contains(Pos(dx, dy)))
        }
    }
}