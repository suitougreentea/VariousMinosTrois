package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.Mino

interface MinoDecorator {
  fun getMino(minoId: Int, colorId: Int): Mino
}