package io.github.suitougreentea.various_minos_trois

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.Align
import io.github.suitougreentea.various_minos_trois.game.Game
import io.github.suitougreentea.various_minos_trois.game.Renderer
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombDebug
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombSurvivalMaster1
import io.github.suitougreentea.various_minos_trois.game.bomb.GameBombSurvivalThanatos1
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagic
import io.github.suitougreentea.various_minos_trois.game.magic.GameMagicDebug
import io.github.suitougreentea.various_minos_trois.rule.RuleModern
import io.github.suitougreentea.various_minos_trois.rule.RuleVariant

class Player(val app: VariousMinosTrois, val screen: GameScreen, val playerNumber: Int) {
  val input = Input(app.keyConfigJson[0])

  var decisionTimer = -1
  var movingDirectionTimer = 0

  var currentGameType = 0
  var previousGameType = 0
  var currentGameMode = 0

  var profile: Profile? = null
  //var profile: Profile? = app.loadProfile("TESTPL")
  var game: Game? = null
  var renderer: Renderer? = null

  val gr = screen.globalRenderer
  val rt = app.renderTool
  val r = app.resources
  val b = rt.spriteBatch

  val normalMatrix = Matrix4()
  init {
    if(playerNumber == 1) normalMatrix.translate(400f, 0f, 0f)
  }
  val fieldMatrix: Matrix4 = normalMatrix.cpy().translate(64f, 88f, 0f)

  fun createDescriptionBuffer(description: String, color: Color): FrameBuffer {
    val glyphLayout = GlyphLayout()
    glyphLayout.setText(r.fJp14, description)
    val width = glyphLayout.width
    val buffer = FrameBuffer(Pixmap.Format.RGBA8888, width.toInt() + 40, 16, false)
    val batch = SpriteBatch()
    buffer.begin()
    Gdx.gl.glClearColor( 0f, 0f, 0f, 0f )
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT )
    batch.begin()
    batch.projectionMatrix = Util.getFrameBufferProjectionMatrix(width + 40f, 16f)
    r.fJp14.color = color
    r.fJp14.draw(batch, description, 20f, 14f)
    r.fJp14.color = Color.WHITE
    batch.end()
    buffer.end()
    return buffer
  }

  fun renderMovingDirectionLR(batch: SpriteBatch, lx: Float, rx: Float, y: Float, timer: Int, left: Boolean = true, right: Boolean = true) {
    val offset = Util.easeOutQuad((timer % 30) / 30f) * 8f - 4f
    batch.draw(r.tDirectionLeft, lx - 6f - offset, y - 10f)
    batch.draw(r.tDirectionRight, rx - 6f + offset, y - 10f)
  }

  fun renderMovingDirectionUD(batch: SpriteBatch, x: Float, uy: Float, dy: Float, timer: Int, left: Boolean = true, right: Boolean = true) {
    val offset = Util.easeOutQuad((timer % 30) / 30f) * 8f - 4f
    batch.draw(r.tDirectionUp, x - 10f, uy - 6f + offset)
    batch.draw(r.tDirectionDown, x - 10f, dy - 6f - offset)
  }

  fun renderDescription(batch: SpriteBatch, buffer: FrameBuffer, y: Float, scissor: Rectangle, timer: Int) {
    val width = buffer.width
    val pos = (timer % width).toFloat()
    batch.flush()
    batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)
    ScissorStack.pushScissors(scissor)
    batch.draw(buffer.colorBufferTexture, -pos, y)
    batch.draw(buffer.colorBufferTexture, width - pos, y)
    batch.flush()
    ScissorStack.popScissors()
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
  }

  val stateManager = StateManager().apply { changeState(StateNoEntry()) }

  fun updateBackground(backgroundId: Int, change: Boolean) {
    gr.updateBackground(playerNumber, backgroundId, change)
  }

  fun update() {
    input.update()

    rt.setTransform(normalMatrix)
    b.begin()
    b.draw(r.tFrame, 48f, 72f)
    b.setColor(0f, 0f, 0f, 0.8f)
    b.draw(r.tFrameBack, 48f, 72f)
    b.color = Color.WHITE

    if(currentRule <= 1) {
      b.draw(r.tNextFrame, 16f, 320f, 0, 0, 288, 216)
      b.setColor(0f, 0f, 0f, 0.8f)
      b.draw(r.tNextFrameBack, 16f, 320f, 0, 0, 288, 216)
      b.color = Color.WHITE
      r.fRoman16.color = FontColor.ORANGE
      r.fRoman16.draw(b, "NEXT", 136f, 536f, 0f, Align.center, false)
      r.fRoman16.draw(b, "HOLD", 52f, 512f, 0f, Align.center, false)
      r.fRoman16.color = FontColor.WHITE
    } else {
      b.draw(r.tNextFrame, 16f, 448f, 0, 216, 288, 88)
      b.setColor(0f, 0f, 0f, 0.8f)
      b.draw(r.tNextFrameBack, 16f, 448f, 0, 216, 288, 88)
      b.color = Color.WHITE
      r.fRoman16.color = FontColor.ORANGE
      r.fRoman16.draw(b, "NEXT", 136f, 536f, 0f, Align.center, false)
      r.fRoman16.color = FontColor.WHITE
    }

    profile?.let {
      r.fRoman24.draw(b, it.name, 24f, 576f)
    }
    b.end()

    stateManager.update()
  }

  open inner class StateNoEntry: State {
    override fun update() {
      if(input.a.isPressed) stateManager.changeState(StatePlayerSelect())

      b.begin()
      rt.setTransform(fieldMatrix)

      if(gr.pressStartTimer < 80) {
        r.fRoman24.setColor(0.8f, 0.8f, 0.8f, 1f)
        r.fRoman24.draw(b, "Player ${playerNumber + 1}", 80f, 176f + 40f, 0f, Align.center, false)
        r.fRoman24.color = Color.WHITE
        r.fRoman32.draw(b, "Press A Button", 80f, 176f + r.fRoman32.capHeight / 2, 0f, Align.center, false)
      }
      b.end()
    }
  }

  open inner class StatePlayerSelect: State {
    override fun update() {
      if(decisionTimer == -1) {
        if (input.a.isPressed) decisionTimer = 0
        if (input.b.isPressed) stateManager.changeState(StateNoEntry())
      } else {
        decisionTimer ++
        if(decisionTimer == 45) {
          decisionTimer = -1
          stateManager.changeState(StateGameSelect())
        }
      }

      b.begin()
      rt.setTransform(fieldMatrix)

      r.fRoman24.color = FontColor.BLUE
      r.fRoman24.draw(b, "Select Player", 80f, 16f*21, 0f, Align.center, false)
      r.fRoman24.color = FontColor.WHITE

      r.fRoman16.color = if(decisionTimer > 0) (if (decisionTimer % 4 < 2) FontColor.ORANGE else FontColor.YELLOW) else FontColor.YELLOW
      r.fRoman16.draw(b, "TESTPL", 16f*2, 16f*19)
      r.fRoman16.color = FontColor.WHITE
      b.end()
    }
  }

  var gameTypeDescriptionTimer = 0
  var gameModeDescriptionTimer = 0

  var gameTypeChangeTimer = -1
  var gameTypeChangeDirection = 0

  val gameTypeBuffer = GameTypeList.list.map {
    val buffer = FrameBuffer(Pixmap.Format.RGBA8888, 160, 80, false)
    val batch = SpriteBatch()
    buffer.begin()
    Gdx.gl.glClearColor( 0f, 0f, 0f, 0f )
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT )
    batch.begin()
    batch.projectionMatrix = Util.getFrameBufferProjectionMatrix(160f, 80f)
    r.fRoman24.color = it.color
    r.fRoman24.draw(batch, it.name, 80f, 30f, 0f, Align.center, false)
    r.fRoman24.color = Color.WHITE
    batch.transformMatrix = Matrix4().translate(64f, 40f, 0f)
    it.thumbnailRenderFunction(batch, r)
    batch.end()
    buffer.end()
    buffer
  }

  val gameTypeDescriptionBuffer = GameTypeList.list.map { createDescriptionBuffer(it.description, it.color) }
  val gameModeDescriptionBuffer = GameTypeList.list.map { it.gameModeList.map { createDescriptionBuffer(it.description, it.color) }}

  val gameTypeScissors = Rectangle().apply {
    ScissorStack.calculateScissors(Util.defaultCamera, fieldMatrix, Rectangle(0f, 16f*13, 160f, 16f + 80f), this)
  }

  val gameModeScissors = Rectangle().apply {
    ScissorStack.calculateScissors(Util.defaultCamera, fieldMatrix, Rectangle(0f, 16f, 160f, 16f), this)
  }

  open inner class StateGameSelect: State {
    override fun enter() {
      profile = app.loadProfile("TESTPL")
      movingDirectionTimer = 0
    }

    override fun update() {
      movingDirectionTimer ++
      gameTypeDescriptionTimer ++
      gameModeDescriptionTimer ++

      if(decisionTimer == -1) {
        if (input.a.isPressed) decisionTimer = 0
        if (input.b.isPressed) {
          profile = null
          stateManager.changeState(StatePlayerSelect())
        }
      } else {
        decisionTimer ++
        if(decisionTimer == 45) {
          decisionTimer = -1
          stateManager.changeState(StateModeSelect())
        }
      }

      if(input.left.isPressed) {
        previousGameType = currentGameType
        currentGameType = (currentGameType + GameTypeList.num - 1) % GameTypeList.num
        gameTypeChangeDirection = -1
        gameTypeChangeTimer = 0
        currentGameMode = 0
        gameTypeDescriptionTimer = 0
        gameModeDescriptionTimer = 0
      }
      if(input.right.isPressed) {
        previousGameType = currentGameType
        currentGameType = (currentGameType + 1) % GameTypeList.num
        gameTypeChangeDirection = 1
        gameTypeChangeTimer = 0
        currentGameMode = 0
        gameTypeDescriptionTimer = 0
        gameModeDescriptionTimer = 0
      }
      if(gameTypeChangeTimer >= 0) {
        gameTypeChangeTimer ++
        if(gameTypeChangeTimer == 30) {
          gameTypeChangeTimer = -1
        }
      }

      b.begin()

      rt.setTransform(fieldMatrix)
      r.fRoman24.color = FontColor.BLUE
      r.fRoman24.draw(b, "Select Game", 80f, 16f*21, 0f, Align.center, false)
      r.fRoman24.color = FontColor.WHITE
      b.flush()

      val gameTypeBuf = gameTypeBuffer[currentGameType]
      val gameTypeDescBuf = gameTypeDescriptionBuffer[currentGameType]

      b.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)
      ScissorStack.pushScissors(gameTypeScissors)
      if(gameTypeChangeTimer < 0) {
        b.draw(gameTypeBuf.colorBufferTexture, 0f, 16f*14)
      } else {
        val gameTypeBufPrev = gameTypeBuffer[previousGameType]
        val offset = (1f - (gameTypeChangeTimer / 30f)) * 160f * gameTypeChangeDirection
        b.draw(gameTypeBuf.colorBufferTexture, offset, 16f*14)
        b.draw(gameTypeBufPrev.colorBufferTexture, offset + 160f * -gameTypeChangeDirection, 16f*14)
      }
      b.flush()
      ScissorStack.popScissors()
      b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

      renderDescription(b, gameTypeDescBuf, 16f*13, gameTypeScissors, gameTypeDescriptionTimer)
      renderMovingDirectionLR(b, 0f, 160f, 16f*14 + 40f, movingDirectionTimer)

      renderMovingDirectionUD(b, 80f, 16f*11, 16f*3 + 8f, movingDirectionTimer)
      val modeList = GameTypeList.list[currentGameType].gameModeList
      modeList.forEachIndexed { i, e ->
        r.fRoman24.color = e.color.cpy().mul(0.5f, 0.5f, 0.5f, 0.5f)
        r.fRoman24.draw(b, e.name, 80f, 16f * 10 - i * 16f, 0f, Align.center, false)
        r.fRoman24.draw(b, e.name, 80f, 16f * 6 - i * 16f, 0f, Align.center, false)
        r.fRoman24.color = if(decisionTimer > 0) (if (decisionTimer % 4 < 2) FontColor.ORANGE else FontColor.YELLOW) else e.color
        r.fRoman24.draw(b, e.name, 80f, 16f * 8 - i * 16f, 0f, Align.center, false)
      }
      r.fRoman24.color = Color.WHITE

      val gameModeDescBuf = gameModeDescriptionBuffer[currentGameType][currentGameMode]
      renderDescription(b, gameModeDescBuf, 16f, gameModeScissors, gameModeDescriptionTimer)

      b.end()
    }
  }

  var gameModeDetailedDescriptionBuffer: List<FrameBuffer>? = null
  var gameModeDetailedDescriptionTimer = 0

  var ruleDescriptionTimer = 0

  val gameModeDetailedScissors = Rectangle().apply {
    ScissorStack.calculateScissors(Util.defaultCamera, fieldMatrix, Rectangle(0f, 16f*10, 160f, 16f), this)
  }

  val ruleScissors = Rectangle().apply {
    ScissorStack.calculateScissors(Util.defaultCamera, fieldMatrix, Rectangle(0f, 16f, 160f, 16f), this)
  }

  val ruleDescriptionBuffer = RuleList.list.map { createDescriptionBuffer(it.description, it.color) }

  var currentModeDetailed = 0
  var currentRule = 0

  open inner class StateModeSelect: State {
    override fun enter() {
      movingDirectionTimer = 0
      gameModeDescriptionTimer = 0
      ruleDescriptionTimer = 0
      gameModeDetailedDescriptionBuffer = GameTypeList.list[currentGameType].gameModeList[currentGameMode].gameModeDetailedList.map { createDescriptionBuffer(it.description, it.color) }
    }

    override fun update() {
      val currentGameMode = GameTypeList.list[currentGameType].gameModeList[currentGameMode]
      val list = currentGameMode.gameModeDetailedList
      movingDirectionTimer ++
      gameModeDetailedDescriptionTimer ++
      ruleDescriptionTimer ++

      if(decisionTimer == -1) {
        if(input.a.isPressed) decisionTimer = 0
        if(input.b.isPressed) stateManager.changeState(StateGameSelect())
      } else {
        decisionTimer ++
        if(decisionTimer == 45) {
          decisionTimer = -1
          stateManager.changeState(StateInGame())
        }
      }
      if(input.left.isPressed) {
        currentRule = (currentRule + 3) % 4
        ruleDescriptionTimer = 0
      }
      if(input.right.isPressed) {
        currentRule = (currentRule + 1) % 4
        ruleDescriptionTimer = 0
      }
      if(input.up.isPressed) {
        currentModeDetailed = (currentModeDetailed + list.size - 1) % list.size
        gameModeDetailedDescriptionTimer = 0
      }
      if(input.down.isPressed) {
        currentModeDetailed = (currentModeDetailed + 1) % list.size
        gameModeDetailedDescriptionTimer = 0
      }

      b.begin()

      rt.setTransform(fieldMatrix)
      r.fRoman24.color = FontColor.BLUE
      r.fRoman24.draw(b, "Select Mode", 80f, 16f*21, 0f, Align.center, false)
      r.fRoman24.draw(b, "RuleEntry", 80f, 16f*8, 0f, Align.center, false)

      r.fRoman16.color = currentGameMode.color
      r.fRoman16.draw(b, currentGameMode.name, 80f, 16f*19 + 8f, 0f, Align.center, false)
      r.fRoman16.color = FontColor.WHITE

      val currentMode = list[currentModeDetailed % list.size]
      val prevMode = list[(currentModeDetailed + list.size - 1) % list.size]
      val nextMode = list[(currentModeDetailed + 1) % list.size]
      r.fRoman24.color = if(decisionTimer > 0) (if (decisionTimer % 4 < 2) FontColor.ORANGE else FontColor.YELLOW) else currentMode.color
      r.fRoman24.draw(b, currentMode.name, 80f, 16f*15f + 8f, 0f, Align.center, false)
      r.fRoman24.color = prevMode.color.cpy().mul(0.5f, 0.5f, 0.5f, 0.5f)
      r.fRoman24.draw(b, prevMode.name, 80f, 16f*15f + 32f, 0f, Align.center, false)
      r.fRoman24.color = nextMode.color.cpy().mul(0.5f, 0.5f, 0.5f, 0.5f)
      r.fRoman24.draw(b, nextMode.name, 80f, 16f*15f - 16f, 0f, Align.center, false)

      renderMovingDirectionUD(b, 80f, 16f*17 + 6f, 16f*12 + 6f, movingDirectionTimer)

      val gameModeDescBuf = gameModeDetailedDescriptionBuffer?.get(currentModeDetailed) ?: throw IllegalStateException()
      renderDescription(b, gameModeDescBuf, 16f*10, gameModeDetailedScissors, gameModeDetailedDescriptionTimer)

      (0..3).forEach {
        b.draw(r.tRuleIcon, 24f + 32f * it, 80f, it * 16, if(it == currentRule) 0 else 16, 16, 16)
      }

      val rule = RuleList.list[currentRule]
      r.fRoman24.color = if(decisionTimer > 0) (if (decisionTimer % 4 < 2) FontColor.ORANGE else FontColor.YELLOW) else rule.color
      r.fRoman24.draw(b, rule.name, 80f, 16f*4, 0f, Align.center, false)
      r.fRoman24.color = Color.WHITE
      renderMovingDirectionLR(b, 0f, 160f, 88f, movingDirectionTimer)

      val ruleDescBuf = ruleDescriptionBuffer[currentRule]
      renderDescription(b, ruleDescBuf, 16f, ruleScissors, ruleDescriptionTimer)

      b.end()
    }

    override fun leave() {
      gameModeDetailedDescriptionBuffer?.forEach { it.dispose() }
    }
  }

  open inner class StateInGame: State {
    override fun enter() {
      val gameName = GameTypeList.list[currentGameType].gameModeList[currentGameMode].gameModeDetailedList[currentModeDetailed].internalName
      val ruleName = RuleList.list[currentRule].internalName
      val rule = when(ruleName) {
        "modern" -> RuleModern()
        "variant" -> RuleVariant()
        "classic" -> RuleModern()
        "retro" -> RuleModern()
        else -> throw IllegalArgumentException()
      }
      game = when(gameName) {
        "bomb.debug"     -> GameBombDebug            (this@Player, rule)
        "bomb.proson1"   -> GameBombSurvivalMaster1  (this@Player, rule)
        "bomb.thanatos1" -> GameBombSurvivalThanatos1(this@Player, rule)
        "magic.debug"    -> GameMagicDebug           (this@Player, rule)
        else -> throw IllegalArgumentException()
      }.also {
        renderer = it.getRequiredRenderer(app)
        it.init()
      }
    }
    override fun update() {
      game?.let {
        it.update()
        renderer?.render(it)
      } ?: throw IllegalStateException()
    }
  }
}
