package io.github.suitougreentea.various_minos_trois

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