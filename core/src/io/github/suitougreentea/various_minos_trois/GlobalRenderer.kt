package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GlobalRenderer(val app: VariousMinosTrois) {
  val r = app.resources

  val b = app.renderTool.spriteBatch

  val requestedBackground = arrayOf(0, 0)
  var actualBackground = 0

  val frameType = arrayOf(0, 0)
  val frameColor = arrayOf(Color.WHITE, Color.WHITE)

  var pressStartTimer = 0

  fun updateBackground(playerNumber: Int, backgroundId: Int, change: Boolean) {
    requestedBackground[playerNumber] = backgroundId
    if(change) {
      actualBackground = requestedBackground.max() ?: 0
    }
  }

  fun render() {
    pressStartTimer = (pressStartTimer + 1) % 120
    b.begin()
    b.draw(r.tBackgrounds[actualBackground], 0f, 0f)
    b.end()
  }
}

