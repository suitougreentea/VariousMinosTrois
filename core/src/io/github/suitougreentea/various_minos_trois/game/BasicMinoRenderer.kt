package io.github.suitougreentea.various_minos_trois.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.suitougreentea.various_minos_trois.Block
import io.github.suitougreentea.various_minos_trois.GameScreen
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

    renderBackground()
    renderBehindFrame(g)
    renderFrame()
    renderBehindField(g)
    renderField(g)
    renderFieldBorder(g)
    renderAfterField(g)
    renderActiveMino(g)
    renderAfterActiveMino(g)
    renderNextHold(g)
    renderInput(g)
    renderDebugString(g)
    renderTopMost(g)
  }

  fun renderBackground() {
    b.begin()
    b.draw(r.tBackgrounds[0], 0f, 0f)

    b.color = Color(1f, 1f, 1f, 0.2f)
    b.draw(r.tDesign, 0f, 0f)
    b.color = Color.WHITE
    b.end()
  }

  open fun renderBehindFrame(g: BasicMinoGame) {}

  fun renderFrame() {
    b.begin()
    b.draw(r.tFrame, 152f, 72f)
    b.end()
  }

  open fun renderBehindField(g: BasicMinoGame) {}

  fun renderField(g: BasicMinoGame) {
    b.begin()
    g.field.map.filter { it.key.y < 22 }.forEach {
      val (dx, dy) = getBlockCoord(it.key)
      renderBlock(b, it.value, dx, dy)
    }
    b.end()
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

  fun renderFieldBorder(g: BasicMinoGame) {
    s.begin(ShapeRenderer.ShapeType.Filled)
    Gdx.gl.glEnable(GL_BLEND)
    Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    s.setColor(1f, 1f, 1f, 0.7f)
    g.field.map.filter { it.key.y < 22 }.forEach {
      val (x, y) = it.key
      val (dx, dy) = getBlockCoord(it.key)
      if(x - 1 >= 0 && !g.field.contains(Pos(x - 1, y))) s.rectLine(dx, dy, dx, dy + 16f, 2f)
      if(x + 1 < g.width && !g.field.contains(Pos(x + 1, y))) s.rectLine(dx + 16f, dy, dx + 16f, dy + 16f, 2f)
      if(y - 1 >= 0 && !g.field.contains(Pos(x, y - 1))) s.rectLine(dx, dy, dx + 16f, dy, 2f)
      if(y + 1 < 22 && !g.field.contains(Pos(x, y + 1))) s.rectLine(dx, dy + 16f, dx + 16f, dy + 16f, 2f)
    }
    s.end()
  }

  open fun renderAfterField(g: BasicMinoGame) {}

  fun getBlockCoord(x: Int, y: Int) = Pair(168f + x * 16f, 88f + y * 16f)

  fun getBlockCoord(c: Pos) = getBlockCoord(c.x, c.y)

  open fun getBlockSourceCoord(b: Block) = Pair(1, 0)

  fun renderNextHold(g: BasicMinoGame) {
    b.begin()
    nextPositions.forEachIndexed { i, e ->
      if(g.minoBuffer.size > i) {
        val (ox, oy) = e.first
        val size = e.second
        val mino = g.minoBuffer[i]
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
    b.end()
  }

  fun renderActiveMino(g: BasicMinoGame) {
    val mino = g.currentMino ?: return
    if (g.stateManager.currentState is BasicMinoGame.StateMoving) {
      b.begin()
      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.ghostY + e.first.y)
        renderBlock(b, e.second, dx, dy, t = 0.5f)
      }

      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.minoY + e.first.y)
        renderBlock(b, e.second, dx, dy)
      }
      b.end()

      s.begin(ShapeRenderer.ShapeType.Filled)
      Gdx.gl.glEnable(GL_BLEND)
      Gdx.gl.glBlendFunc(GL_ONE, GL_ONE)
      val t = (1f - g.lockTimer.toFloat() / g.speed.lock) * 0.2f
      s.setColor(t, t, t, 1f)
      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.minoY + e.first.y)
        s.rect(dx, dy, 16f, 16f)
      }
      s.end()
      Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

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

  open fun renderAfterActiveMino(g: BasicMinoGame) {}

  fun prettifyBoolean(boolean: Boolean) = if(boolean) "*" else "."
  fun renderInput(g: BasicMinoGame) {
    b.begin()
    r.fDebug14.draw(b, g.input.mapping.keys.map { e -> "%5s: %s %s %s".format(e.name, prettifyBoolean(e.isPressed), prettifyBoolean(e.isDown), prettifyBoolean(e.isReleased)) }.joinToString("\n"), 680f, 200f)
    b.end()
  }

  open fun renderTopMost(g: BasicMinoGame) {}

  open fun getDebugString(g: BasicMinoGame): String {
    val currentState = g.stateManager.currentState
    val stringBuilder = StringBuilder()
    stringBuilder.apply {
      appendln(currentState.javaClass.simpleName)
      if (currentState is StateWithTimer) appendln("-> ${currentState.timer} / ${currentState.frames}") else appendln()
      appendln("mino: ${g.currentMino?.minoId}")
      appendln("x: ${g.minoX}")
      appendln("y: ${g.minoY}")
      appendln("r: ${g.minoR}")
      appendln("moveDir: ${g.moveDirection}")
      appendln("moveTimer: ${g.moveTimer}")
      appendln("moveStack: ${g.moveStack}")
      appendln("dropStack: ${g.dropStack}")
      appendln("softStack: ${g.softDropStack}")
      appendln("lock: ${g.lockTimer}")
      appendln("forceLock: ${g.forceLockTimer}")
      appendln("cascade: ${g.cascadeStack}")
    }
    return stringBuilder.toString()
  }

  fun renderDebugString(g: BasicMinoGame) {
    b.begin()
    r.fDebug14.draw(b, getDebugString(g), 400f, 584f)
    r.fDebug14.draw(b, "${Gdx.graphics.framesPerSecond} FPS", 16f, 584f)
    b.end()
  }
}