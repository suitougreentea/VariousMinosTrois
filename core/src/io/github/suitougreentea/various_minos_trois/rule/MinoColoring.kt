package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.Block
import io.github.suitougreentea.various_minos_trois.BlockNormal
import io.github.suitougreentea.various_minos_trois.Pos

interface MinoColoring {
    fun getColoredBlocks(minoId: Int, positions: List<Pos>): List<Pair<Pos, Block>>
}

class MinoColoringStandard: MinoColoring {
    val color = arrayOf(
            20, 20, 20, 8,
            20, 26, 5, 8, 14, 32, 2,
            14, 2, 20, 5, 26, 2, 14, 26, 5, 32, 8, 8, 8, 8, 32, 32, 2, 14)
    override fun getColoredBlocks(minoId: Int, positions: List<Pos>): List<Pair<Pos, Block>> {
        val color = color[minoId]
        return positions.map { Pair(it, BlockNormal(color)) }
    }
}
