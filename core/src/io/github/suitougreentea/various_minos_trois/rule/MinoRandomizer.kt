package io.github.suitougreentea.various_minos_trois.rule

import java.util.*

interface MinoRandomizer {
  fun newMinoSet(minoSet: Set<Int>)
  fun getMinoSet(): Set<Int>
  fun next(): Int
  fun reset()
}
class MinoRandomizerRandom(): MinoRandomizer {
  private var minoSet: Set<Int> = setOf()

  override fun getMinoSet(): Set<Int> = minoSet

  override fun next(): Int {
    return minoSet.toList()[(Math.random() * minoSet.size).toInt()]
  }

  override fun reset() {
    throw UnsupportedOperationException()
  }

  override fun newMinoSet(minoSet: Set<Int>) {
    this.minoSet = minoSet
  }
}

class MinoRandomizerBag(): MinoRandomizer {
  private var minoSet: Set<Int> = setOf()
  private var bag: MutableSet<Int> = HashSet(minoSet)

  override fun getMinoSet(): Set<Int> = minoSet

  override fun newMinoSet(minoSet: Set<Int>) {
    this.minoSet = minoSet
    this.bag = HashSet(minoSet)
  }

  override fun next(): Int {
    if(bag.size == 0) {
      if(minoSet.size == 0) throw IllegalArgumentException()
      bag = HashSet(minoSet)
    }
    val list = bag.toList()
    val result = list[(Math.random() * list.size).toInt()]
    bag.remove(result)
    return result
  }

  override fun reset() {
    throw UnsupportedOperationException()
  }
}
