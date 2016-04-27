package io.github.suitougreentea.various_minos_trois


class GameScreen(val game: VariousMinosTrois) {
    val renderer = Renderer(game)
    val input = Input()
    val field = Game(input, 10, 30)

    fun render() {
        input.update()
        field.update()
        renderer.render(field)
    }
}