package io.github.suitougreentea.various_minos_trois

class Mino (val minoId: Int, decorator: MinoDecorator){
    val size: Int
    val blocks: List<Pair<Pos, Block>>
    init {
        val data = MinoList.list[minoId]
        size = data.first
        blocks = decorator.decorate(data.second, minoId)
    }

    fun getRotatedBlocks(rotation: Int) = blocks.map { e -> Pair(MinoCoordinateHelper.getRotatedCoordinate(e.first, rotation, size), e.second) }
    fun getRotatedRectangle(rotation: Int): Pair<Pos, Pos> {
        val blockCoords = blocks.map { e -> MinoCoordinateHelper.getRotatedCoordinate(e.first, rotation, size) }
        val xCoords = blockCoords.map { it.x }
        val yCoords = blockCoords.map { it.y }
        return Pair(Pos(xCoords.min() ?: 0, yCoords.min() ?: 0), Pos(xCoords.max() ?: size-1, yCoords.max() ?: size-1))
    }
}

object MinoCoordinateHelper {
    fun getRotatedCoordinate(coordinate: Pos, rotation: Int, size: Int) = when (rotation) {
        0 -> Pos(coordinate.x, coordinate.y)
        1 -> Pos(coordinate.y, size-1 - coordinate.x)
        2 -> Pos(size-1 - coordinate.x, size-1 - coordinate.y)
        3 -> Pos(size-1 - coordinate.y, coordinate.x)
        else -> throw IllegalArgumentException()
    }
    fun getRotatedRectangle(minoId: Int, rotation: Int): Pair<Pos, Pos> {
        val (size, coords) = MinoList.list[minoId]
        val blockCoords = coords.map { MinoCoordinateHelper.getRotatedCoordinate(it, rotation, size) }
        val xCoords = blockCoords.map { it.x }
        val yCoords = blockCoords.map { it.y }
        return Pair(Pos(xCoords.min() ?: 0, yCoords.min() ?: 0), Pos(xCoords.max() ?: size-1, yCoords.max() ?: size-1))
    }
}

interface MinoDecorator {
    fun decorate(blockCoordinates: List<Pos>, minoId: Int): List<Pair<Pos, Block>>
}

class MinoDecoratorDefault(): MinoDecorator {
    override fun decorate(blockCoordinates: List<Pos>, minoId: Int): List<Pair<Pos, Block>> {
        val bombIndex = (Math.random() * blockCoordinates.size).toInt()
        return blockCoordinates.mapIndexed { i, pos ->
            when(i) {
                bombIndex -> Pair(pos, BlockBomb())
                else -> Pair(pos, BlockNormal(1))
                //else -> Pair(pos, BlockBomb())
            }
        }
    }
}

object MinoList {
    val list = listOf(
        Pair(1, listOf(Pos(0, 0))),  // 0: 1
        Pair(2, listOf(Pos(0, 1), Pos(1, 1))),  // 1: 2
        Pair(3, listOf(Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 2: 3I
        Pair(2, listOf(Pos(0, 1), Pos(0, 0), Pos(1, 0))),  // 3: 3L
        Pair(4, listOf(Pos(0, 2), Pos(1, 2), Pos(2, 2), Pos(3, 2))),  // 4: 4I
        Pair(3, listOf(Pos(0, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 5: 4J
        Pair(3, listOf(Pos(2, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 6: 4L
        Pair(2, listOf(Pos(0, 1), Pos(1, 1), Pos(0, 0), Pos(1, 0))),  // 7: 4O
        Pair(3, listOf(Pos(1, 2), Pos(2, 2), Pos(0, 1), Pos(1, 1))),  // 8: 4S
        Pair(3, listOf(Pos(1, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 9: 4T
        Pair(3, listOf(Pos(0, 2), Pos(1, 2), Pos(1, 1), Pos(2, 1))),  // 10: 4Z
        Pair(3, listOf(Pos(1, 2), Pos(1, 1), Pos(2, 1), Pos(0, 0), Pos(1, 0))),  // 11: 5F
        Pair(3, listOf(Pos(1, 2), Pos(0, 1), Pos(1, 1), Pos(1, 0), Pos(2, 0))),  // 12: 5FM
        Pair(5, listOf(Pos(0, 2), Pos(1, 2), Pos(2, 2), Pos(3, 2), Pos(4, 2))),  // 13: 5I
        Pair(4, listOf(Pos(3, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1), Pos(3, 1))),  // 14: 5L
        Pair(4, listOf(Pos(0, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1), Pos(3, 1))),  // 15: 5LM
        Pair(4, listOf(Pos(0, 2), Pos(1, 2), Pos(1, 1), Pos(2, 1), Pos(3, 1))),  // 16: 5N
        Pair(4, listOf(Pos(2, 2), Pos(3, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 17: 5NM
        Pair(3, listOf(Pos(0, 2), Pos(1, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 18: 5P
        Pair(3, listOf(Pos(1, 2), Pos(2, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 19: 5PM
        Pair(3, listOf(Pos(1, 2), Pos(1, 1), Pos(0, 0), Pos(1, 0), Pos(2, 0))),  // 20: 5T
        Pair(3, listOf(Pos(0, 2), Pos(2, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1))),  // 21: 5U
        Pair(3, listOf(Pos(0, 2), Pos(0, 1), Pos(0, 0), Pos(1, 0), Pos(2, 0))),  // 22: 5V
        Pair(3, listOf(Pos(0, 2), Pos(0, 1), Pos(1, 1), Pos(1, 0), Pos(2, 0))),  // 23: 5W
        Pair(3, listOf(Pos(1, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1), Pos(1, 0))),  // 24: 5X
        Pair(4, listOf(Pos(2, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1), Pos(3, 1))),  // 25: 5Y
        Pair(4, listOf(Pos(1, 2), Pos(0, 1), Pos(1, 1), Pos(2, 1), Pos(3, 1))),  // 26: 5YM
        Pair(3, listOf(Pos(0, 2), Pos(1, 2), Pos(1, 1), Pos(1, 0), Pos(2, 0))),  // 27: 5Z
        Pair(3, listOf(Pos(1, 2), Pos(2, 2), Pos(1, 1), Pos(0, 0), Pos(1, 0)))  // 28: 5ZM
    )
}