package io.github.suitougreentea.various_minos_trois

import io.github.suitougreentea.various_minos_trois.game.Game
import io.github.suitougreentea.various_minos_trois.game.Renderer
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBomb
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombSurvival
import io.github.suitougreentea.various_minos_trois.game.bomb.RendererBomb
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagic
import io.github.suitougreentea.various_minos_trois.game.magic.RendererMagic
import java.util.*


class GameScreen(val app: VariousMinosTrois) {
    val input = Input()

    //val renderer: Renderer = RendererBomb(app)
    //val game: Game = GameBombSurvival(input)
    val renderer: Renderer = RendererMagic(app)
    val game: Game = GameMagic(input)

    fun render() {
        input.update()
        game.update()
        renderer.render(game)
    }
}
