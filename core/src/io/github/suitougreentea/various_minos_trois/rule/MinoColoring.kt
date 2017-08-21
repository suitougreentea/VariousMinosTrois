package io.github.suitougreentea.various_minos_trois.rule

import io.github.suitougreentea.various_minos_trois.Pos

interface MinoColoring {
  fun getMinoColor(minoId: Int): Int
}

class MinoColoringStandard: MinoColoring {
  val color = arrayOf(
    20, 20, 20, 8,
    20, 26, 5, 8, 14, 32, 2,
    14, 2, 20, 5, 26, 2, 14, 26, 5, 32, 8, 8, 8, 8, 32, 32, 2, 14)
  override fun getMinoColor(minoId: Int): Int {
    return color[minoId]
  }
}

class MinoColoringVariant: MinoColoring {
  val color = arrayOf(
      2, 2, 2, 8,
      2, 26, 5, 8, 32, 20, 14,
      32, 14, 2, 5, 26, 14, 32, 26, 5, 20, 8, 8, 8, 8, 20, 20, 14, 32)
  override fun getMinoColor(minoId: Int): Int {
    return color[minoId]
  }
}

class MinoColoringRetro(val colorId: Int): MinoColoring {
  override fun getMinoColor(minoId: Int): Int {
    return colorId
  }
}
