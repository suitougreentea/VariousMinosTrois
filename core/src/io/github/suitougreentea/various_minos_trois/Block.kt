package io.github.suitougreentea.various_minos_trois

interface Block {
    val float: Boolean
}

class BlockNormal(val color: Int): Block {
    override val float = false
}

class BlockBomb: Block {
    override val float = false
    var ignited = false
}

class BlockBigBomb(val position: Position): Block {
    enum class Position {
        BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT
    }
    override val float = false
    var ignited = false
}

class BlockWhite(var level: Int): Block {
    override val float = false
}

class BlockWhiteUnbreakable(): Block {
    override val float = false
}

class BlockBlack(var level: Int): Block {
    override val float = true
}

class BlockBlackUnbreakable(): Block {
    override val float = true
}
