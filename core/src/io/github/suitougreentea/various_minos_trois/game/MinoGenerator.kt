package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.Mino

interface MinoGenerator {
  fun newMino(): Mino
}