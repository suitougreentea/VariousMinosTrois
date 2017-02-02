package io.github.suitougreentea.various_minos_trois.game.magic

import io.github.suitougreentea.various_minos_trois.Field
import io.github.suitougreentea.various_minos_trois.Input
import io.github.suitougreentea.various_minos_trois.Mino
import io.github.suitougreentea.various_minos_trois.MinoList
import io.github.suitougreentea.various_minos_trois.game.BasicMinoGame
import io.github.suitougreentea.various_minos_trois.game.MinoDecorator
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBomb
import io.github.suitougreentea.various_minos_trois.rule.MinoGenerator
import io.github.suitougreentea.various_minos_trois.rule.MinoGeneratorInfinite

open class GameMagic(input: Input): BasicMinoGame(input, 10, 50) {
  val minoDecorator = object: MinoDecorator {
    override fun getMino(minoId: Int, colorId: Int): Mino {
      val blockPositions = MinoList.list[minoId].second
      val blocks = blockPositions.mapIndexed { i, pos -> Pair(pos, GameBomb.BlockNormal(colorId)) }

      return Mino(minoId, blocks)
    }
  }
  override val minoGenerator = MinoGeneratorInfinite(6, minoRandomizer, minoColoring, minoDecorator)
}