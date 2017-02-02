package io.github.suitougreentea.various_minos_trois.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.suitougreentea.various_minos_trois.Block
import io.github.suitougreentea.various_minos_trois.Pos
import io.github.suitougreentea.various_minos_trois.VariousMinosTrois

open class BasicMinoRenderer(val app: VariousMinosTrois): Renderer {
  val r = app.resources

  val b = SpriteBatch()
  val s = ShapeRenderer()
  init {
    s.projectionMatrix = b.projectionMatrix
    s.transformMatrix = b.transformMatrix

    with(app.resources.mPlay1) {
      isLooping = true
      //play()
    }
  }

  val nextPositions = arrayOf(
          Pair(Pair(218, 468), 16),
          Pair(Pair(304, 472), 8),
          Pair(Pair(354, 472), 8),
          Pair(Pair(354, 440), 8),
          Pair(Pair(354, 408), 8),
          Pair(Pair(354, 376), 8)
  )
  val holdPosition = Pair(Pair(168, 472), 8)

  override fun render(g: Game) {
    if(g !is BasicMinoGame) return

    b.begin()
    renderBackground()
    renderFrame()
    renderField(g)
    renderNextHold(g)

    renderActiveMino(g)
    b.end()
  }

  fun renderBackground() {
    b.draw(r.tBackgrounds[0], 0f, 0f)

    b.color = Color(1f, 1f, 1f, 0.2f)
    b.draw(r.tDesign, 0f, 0f)
    b.color = Color.WHITE
  }

  fun renderFrame() {
    b.draw(r.tFrame, 152f, 72f)
  }

  fun renderField(g: BasicMinoGame) {
    g.field.map.filter { it.key.y < 22 }.forEach {
      val (dx, dy) = getBlockCoord(it.key)
      renderBlock(b, it.value, dx, dy)
    }
  }

  fun renderBlock(batch: SpriteBatch, b: Block, x: Float, y: Float, s: Int = 16, t: Float = 1f) {
    val (sx, sy) = getBlockSourceCoord(b)
    renderBlock(batch, sx, sy, x, y, s, t)
  }

  fun renderBlock(batch: SpriteBatch, sx: Int, sy: Int, x: Float, y: Float, s: Int = 16, t: Float = 1f) {
    batch.setColor(1f, 1f, 1f, t)
    batch.draw(r.tBlock, x, y, s.toFloat(), s.toFloat(), sx * 16, sy * 16, 16, 16, false, false)
    batch.color = Color.WHITE
  }

  fun getBlockCoord(x: Int, y: Int) = Pair(168f + x * 16f, 88f + y * 16f)

  fun getBlockCoord(c: Pos) = getBlockCoord(c.x, c.y)

  open fun getBlockSourceCoord(b: Block) = Pair(1, 0)

  fun renderNextHold(g: BasicMinoGame) {
    nextPositions.forEachIndexed { i, e ->
      if(g.minoGenerator.size > i) {
        val (ox, oy) = e.first
        val size = e.second
        val mino = g.minoGenerator[i]
        if(mino != null) {
          mino.blocks.forEach {
            val (bx, by) = it.first
            val block = it.second
            renderBlock(b, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
          }
        }
      }
    }

    val holdMino = g.holdMino
    if(holdMino != null) {
      val (ox, oy) = holdPosition.first
      val size = holdPosition.second
      holdMino.blocks.forEach { f ->
        val (bx, by) = f.first
        val block = f.second
        renderBlock(b, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
      }
    }
  }

  fun renderActiveMino(g: BasicMinoGame) {
    val mino = g.currentMino ?: return
    if (g.stateManager.currentState is BasicMinoGame.StateMoving) {
      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.ghostY + e.first.y)
        renderBlock(b, e.second, dx, dy, t = 0.5f)
      }

      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.minoY + e.first.y)
        renderBlock(b, e.second, dx, dy)
      }

      /*
  s.begin(ShapeRenderer.ShapeType.Line)
  run {
      val mino = g.currentMino
      val (dx, dy) = getBlockCoord(g.minoX, g.minoY)
      s.color = Color.WHITE
      s.rect(dx, dy, mino.size * 16f, mino.size * 16f)
  }
  run {
      val mino = g.currentMino
      val rect = mino.getRotatedRectangle(g.minoR)
      val (dx, dy) = getBlockCoord(g.minoX + rect.first.x, g.minoY + rect.first.y)
      val w = rect.second.x - rect.first.x + 1
      val h = rect.second.y - rect.first.y + 1
      s.color = Color.PINK
      s.rect(dx, dy, w * 16f, h * 16f)
  }
  s.end()
  */
    }
  }
}