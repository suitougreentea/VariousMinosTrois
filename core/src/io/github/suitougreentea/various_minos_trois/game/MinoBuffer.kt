package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.MinoList
import io.github.suitougreentea.various_minos_trois.game.MinoGenerator
import io.github.suitougreentea.various_minos_trois.rule.MinoColoring
import io.github.suitougreentea.various_minos_trois.rule.MinoColoringStandard
import io.github.suitougreentea.various_minos_trois.rule.MinoRandomizer
import java.util.*

interface MinoBuffer {
  operator fun get(index: Int): Mino?
  operator fun set(index: Int, mino: Mino)
  fun peek(): Mino?
  fun poll(): Mino?
  fun isInfinite(): Boolean
  val size: Int
}

class MinoBufferInfinite(override val size: Int, val generator: MinoGenerator): MinoBuffer {
  val list: MutableList<Mino> = ArrayList()
  init {
    repeat(size) { offer() }
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
    list.add(generator.newMino())
  }

  override fun isInfinite() = true
}

class MinoBufferFinite(override val size: Int, val generator: MinoGenerator, val randomizer: MinoRandomizer, val coloring: MinoColoring): MinoBuffer {
  val list: MutableList<Mino> = ArrayList(MinoBufferInfinite(size, generator).list)

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
}