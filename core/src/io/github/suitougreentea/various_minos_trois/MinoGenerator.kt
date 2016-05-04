package io.github.suitougreentea.various_minos_trois

import java.util.*

interface MinoGenerator {
    operator fun get(index: Int): Mino?
    operator fun set(index: Int, mino: Mino)
    fun peek(): Mino?
    fun poll(): Mino?
    fun isInfinite(): Boolean
    val size: Int
}

class MinoGeneratorInfinite(override val size: Int, val randomizer: MinoRandomizer, val coloring: MinoColoring): MinoGenerator {
    val list: MutableList<Mino> = ArrayList()
    init {
        kotlin.repeat(size) { offer() }
    }

    override fun get(index: Int) = list.getOrNull(index)

    override fun set(index: Int, mino: Mino) {
        if(size < 0 || index >= size) throw IndexOutOfBoundsException()
        list[index] = mino
    }

    override fun peek() = list.getOrNull(0)

    override fun poll(): Mino? {
        val result = list.getOrNull(0)
        if(result != null) list.removeAt(0)
        offer()
        return result
    }

    fun offer() {
        val minoId = randomizer.next()
        val coloredBlocks = coloring.getColoredBlocks(minoId, MinoList.list[minoId].second)

        val bombIndex = (Math.random() * coloredBlocks.size).toInt()
        val blocks = coloredBlocks.mapIndexed { i, pair ->
            when(i) {
                bombIndex -> Pair(pair.first, BlockBomb())
                else -> pair
                //else -> Pair(pos, BlockBomb())
            }
        }

        list.add(Mino(minoId, blocks))
    }

    override fun isInfinite() = true
}

class MinoGeneratorFinite(): MinoGenerator {
    val list: MutableList<Mino> = ArrayList(MinoGeneratorInfinite(6, MinoRandomizerBag(setOf(0, 1, 2, 3, 4)), MinoColoringStandard()).list)

    override fun get(index: Int) = list.getOrNull(index)

    override fun set(index: Int, mino: Mino) {
        if(size < 0 || index >= size) throw IndexOutOfBoundsException()
        list[index] = mino
    }

    override fun peek() = list.getOrNull(0)

    override fun poll(): Mino? {
        val result = list.getOrNull(0)
        if(result != null) list.removeAt(0)
        return result
    }

    override fun isInfinite() = false

    override val size = list.size
}