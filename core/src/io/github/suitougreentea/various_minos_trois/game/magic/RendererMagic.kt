package io.github.suitougreentea.various_minos_trois.game.magic

import io.github.suitougreentea.various_minos_trois.Block
import io.github.suitougreentea.various_minos_trois.VariousMinosTrois
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

    b.begin()

    renderBackground()
    renderFrame()
    renderField(g)
    renderNextHold(g)

    renderActiveMino(g)

    renderInput(g)
    renderDebugString(g) {
      appendln("magicColor: ${g.currentMagicColor}")
      appendln("magicRotation: ${g.currentMagicRotation}")
      appendln("chain: ${g.chain}")
      appendln("allCascade: ${g.allCascade}")
    }

    b.end()

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
}
