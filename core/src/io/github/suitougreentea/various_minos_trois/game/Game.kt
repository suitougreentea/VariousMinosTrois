package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.VariousMinosTrois

interface Game {
  fun init()
  fun update()
  fun getRequiredRenderer(app: VariousMinosTrois): Renderer
}