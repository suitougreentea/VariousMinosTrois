package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.MinoCoordinateHelper
import io.github.suitougreentea.various_minos_trois.MinoList
import io.github.suitougreentea.various_minos_trois.Pos

interface SpawnSystem {
    fun get(minoId: Int): SpawnData
}

class SpawnSystemStandard(val width: Int, val viewHeight: Int): SpawnSystem {
    override fun get(minoId: Int): SpawnData {
        val rectangle = MinoCoordinateHelper.getRotatedRectangle(minoId, 0)
        //val w = rectangle.second.x - rectangle.first.x + 1
        //val h = rectangle.second.y - rectangle.first.y + 1
        val w = MinoList.list[minoId].first
        return SpawnData(Pos((width - w) / 2, viewHeight - rectangle.first.y), 0)
    }
}

data class SpawnData(val position: Pos, val rotation: Int)

