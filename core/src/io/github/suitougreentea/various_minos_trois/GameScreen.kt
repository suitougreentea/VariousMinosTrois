package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.math.Matrix4

class GameScreen(val app: VariousMinosTrois) {
  val globalRenderer = GlobalRenderer(app)

  val player1 = Player(app, this, 0)
  val player2 = Player(app, this, 1)
  var singleMode = true

  fun update() {
    app.renderTool.setTransform(Matrix4())
    globalRenderer.render()
    player1.update()
    //player2.update()
  }
}
