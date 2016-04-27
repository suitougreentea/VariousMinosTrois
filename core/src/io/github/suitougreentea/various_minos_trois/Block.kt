package io.github.suitougreentea.various_minos_trois

interface Block {
}

class BlockNormal(val color: Int): Block {
}

class BlockBomb: Block {
    var ignited = false
}
class BlockBigBomb(val position: Position): Block {
    enum class Position {
        BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT
    }
    var ignited = false
}