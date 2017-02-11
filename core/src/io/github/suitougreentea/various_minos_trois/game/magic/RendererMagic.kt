package io.github.suitougreentea.various_minos_trois.game.magic

import io.github.suitougreentea.various_minos_trois.Block
import io.github.suitougreentea.various_minos_trois.VariousMinosTrois
import io.github.suitougreentea.various_minos_trois.game.BasicMinoGame
import io.github.suitougreentea.various_minos_trois.game.BasicMinoRenderer
import io.github.suitougreentea.various_minos_trois.game.Game
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagic.*

class RendererMagic(app: VariousMinosTrois): BasicMinoRenderer(app) {
  var timer = 0
  var rainbowPhase = 0
  override fun render(g: Game) {
    if(g !is GameMagic) return

    timer ++
    rainbowPhase = (timer / 2f % 3).toInt()

    super.render(g)

    g.seQueue.forEach {
      when(it) {
      }
    }
    g.seQueue.clear()
  }

  override fun getBlockSourceCoord(b: Block) = when(b) {
    is BlockColored -> Pair(b.color, b.magicColor + 8)
    is BlockRainbow -> Pair(b.color, rainbowPhase + 11)
    is BlockBlack -> Pair(5, 14)
    else -> Pair(1, 8)
  }

  override fun getDebugString(g: BasicMinoGame): String {
    g as GameMagic
    val b = StringBuilder()
    b.appendln("magicColor: ${g.currentMagicColor}")
    b.appendln("magicRotation: ${g.currentMagicRotation}")
    b.appendln("chain: ${g.chain}")
    b.appendln("allCascade: ${g.allCascade}")

    return super.getDebugString(g) + b.toString()
  }
}
