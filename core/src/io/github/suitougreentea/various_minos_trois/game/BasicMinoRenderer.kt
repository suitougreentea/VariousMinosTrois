package io.github.suitougreentea.various_minos_trois.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.*
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
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

  open val blockTexture = r.tBlockBomb

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

    renderBackground(g)
    renderBehindFrame(g)
    renderFrame(g)
    renderBehindField(g)
    renderField(g)
    renderFieldBorder(g)
    renderAfterField(g)
    renderActiveMino(g)
    renderLockEffect(g)
    renderAfterActiveMino(g)
    renderNextHold(g)
    renderStatus(g)
    renderInput(g)
    renderDebugString(g)
    renderTopMost(g)
  }

  fun renderBackground(g: BasicMinoGame) {
    b.begin()
    b.draw(r.tBackgrounds[g.background], 0f, 0f)

    b.color = Color(1f, 1f, 1f, 0.2f)
    //b.draw(r.tDesign, 0f, 0f)
    b.color = Color.WHITE
    b.end()
  }

  open fun renderBehindFrame(g: BasicMinoGame) {}

  fun renderFrame(g: BasicMinoGame) {
    b.begin()
    b.draw(r.tNextBackground, 152f, 336f)
    b.draw(r.tFrame, 152f, 72f)
    b.end()
  }

  open fun renderBehindField(g: BasicMinoGame) {}

  fun renderField(g: BasicMinoGame) {
    b.begin()
    g.field.map.filter { it.key.y < 22 }.forEach {
      val (dx, dy) = getBlockCoord(it.key)
      renderBlock(b, blockTexture, it.value, dx, dy)
    }
    b.end()
  }

  fun renderBlock(batch: SpriteBatch, texture: Texture, b: Block, x: Float, y: Float, s: Int = 16) {
    val (sx, sy) = getBlockSourceCoord(b)
    renderBlock(batch, texture, sx, sy, x, y, s)
  }

  fun renderBlock(batch: SpriteBatch, texture: Texture, sx: Int, sy: Int, x: Float, y: Float, s: Int = 16) {
    batch.draw(texture, x, y, s.toFloat(), s.toFloat(), sx * 16, sy * 16, 16, 16, false, false)
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
            renderBlock(b, blockTexture, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
          }
        }
      }
    }

    val holdMino = g.holdMino
    if(holdMino != null) {
      val (ox, oy) = holdPosition.first
      val size = holdPosition.second
      if(g.alreadyHolded) b.setColor(0.5f, 0.5f, 0.5f, 1f)
      holdMino.blocks.forEach { f ->
        val (bx, by) = f.first
        val block = f.second
        renderBlock(b, blockTexture, block, (ox + bx * size).toFloat(), (oy + by * size).toFloat(), size)
      }
      b.setColor(1f, 1f, 1f, 1f)
    }
    b.end()
  }

  fun renderActiveMino(g: BasicMinoGame) {
    val mino = g.currentMino ?: return
    if (g.stateManager.currentState is BasicMinoGame.StateMoving) {
      b.begin()
      b.setColor(1f, 1f, 1f, 0.5f)
      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.ghostY + e.first.y)
        renderBlock(b, blockTexture, e.second, dx, dy)
      }
      b.setColor(1f, 1f, 1f, 1f)

      mino.getRotatedBlocks(g.minoR).forEach { e ->
        val (dx, dy) = getBlockCoord(g.minoX + e.first.x, g.minoY + e.first.y)
        renderBlock(b, blockTexture, e.second, dx, dy)
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

  fun renderLockEffect(g: BasicMinoGame) {
    if(g.lockRenderTimer >= 0) {
      s.begin(ShapeRenderer.ShapeType.Filled)
      Gdx.gl.glEnable(GL_BLEND)
      Gdx.gl.glBlendFunc(GL_ONE, GL_ONE)
      val t = (1f - g.lockRenderTimer.toFloat() / 4) * 0.6f
      s.setColor(t, t, t, 1f)
      g.lockRenderList.forEach { p ->
        val (dx, dy) = getBlockCoord(p)
        s.rect(dx, dy, 16f, 16f)
      }
      s.end()
      Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }
  }

  open fun renderAfterActiveMino(g: BasicMinoGame) {}

  fun prettifyBoolean(boolean: Boolean) = if(boolean) "*" else "."

  fun prettifyTime(time: Int): String {
    val min = time / 3600
    val sec = (time / 60) % 60
    val cent = (time % 60) * 100 / 60
    return "%02d:%02d:%02d".format(min, sec, cent)
  }

  fun renderInput(g: BasicMinoGame) {
    b.begin()
    r.fDebug14.draw(b, g.input.mapping.keys.map { e -> "%5s: %s %s %s".format(e.name, prettifyBoolean(e.isPressed), prettifyBoolean(e.isDown), prettifyBoolean(e.isReleased)) }.joinToString("\n"), 680f, 200f)
    b.end()
  }

  open fun renderStatus(g: BasicMinoGame) {
    b.begin()
    r.fNum24.draw(b, prettifyTime(g.gameTimer), 152f, 48f, 0f, Align.topLeft, false)
    b.end()
  }

  open fun renderTopMost(g: BasicMinoGame) {}

  open fun getDebugString(g: BasicMinoGame): String {
    val currentState = g.stateManager.currentState
    val stringBuilder = StringBuilder()
    stringBuilder.apply {
      appendln(currentState.javaClass.simpleName)
      if (currentState is StateWithTimer) appendln("-> ${currentState.timer} / ${currentState.frames}") else appendln()
      appendln("gameTimer: ${g.gameTimer}")
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
      appendln("lockRenderTimer: ${g.lockRenderTimer}")
      appendln("drop: ${g.drop}")
    }
    return stringBuilder.toString()
  }

  fun renderDebugString(g: BasicMinoGame) {
    b.begin()
    r.fDebug14.draw(b, getDebugString(g), 400f, 584f)
    r.fDebug14.draw(b, "${Gdx.graphics.framesPerSecond} FPS", 16f, 584f)
    b.end()
  }

  fun renderGrade(id: Int, x: Float, y: Float, size: Float, leftAlign: Boolean) {
    b.begin()
    val grade = GradeList.fullList[id]
    val sw = grade.size
    val cx = if(leftAlign) x + sw / 2f else x - sw / 2f
    val cy = y + 40f
    val dw = sw * size
    val dh = 80f * size
    val dx = cx - dw / 2f
    val dy = cy - dh / 2f
    val sx = grade.index / 8 * 120
    val sy = grade.index % 8 * 80
    b.draw(r.tGrade, dx, dy, dw, dh, sx, sy, sw, 80, false, false)
    b.end()
  }
}