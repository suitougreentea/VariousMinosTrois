package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.MinoList
import io.github.suitougreentea.various_minos_trois.game.MinoDecorator
import io.github.suitougreentea.various_minos_trois.rule.MinoColoring
import io.github.suitougreentea.various_minos_trois.rule.MinoColoringStandard
import java.util.*

interface MinoGenerator {
  operator fun get(index: Int): Mino?
  operator fun set(index: Int, mino: Mino)
  fun peek(): Mino?
  fun poll(): Mino?
  fun isInfinite(): Boolean
  val size: Int
}

class MinoGeneratorInfinite(override val size: Int, val randomizer: MinoRandomizer, val coloring: MinoColoring, val decorator: MinoDecorator): MinoGenerator {
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
    val colorId = coloring.getMinoColor(minoId)
    list.add(decorator.getMino(minoId, colorId))
  }

  override fun isInfinite() = true
}

class MinoGeneratorFinite(override val size: Int, val randomizer: MinoRandomizer, val coloring: MinoColoring, val decorator: MinoDecorator): MinoGenerator {
  val list: MutableList<Mino> = ArrayList(MinoGeneratorInfinite(size, randomizer, coloring, decorator).list)

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