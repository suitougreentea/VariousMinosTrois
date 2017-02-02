package io.github.suitougreentea.various_minos_trois

import java.util.*


class Field(val width: Int, val height: Int) {
  val map: MutableMap<Pos, Block> = HashMap()

  fun clear() = map.clear()
  operator fun get(position: Pos) = map.get(position)
  operator fun set(position: Pos, block: Block) = map.put(position, block)
  operator fun contains(position: Pos) = map.contains(position)
  fun remove(position: Pos) = map.remove(position)
}