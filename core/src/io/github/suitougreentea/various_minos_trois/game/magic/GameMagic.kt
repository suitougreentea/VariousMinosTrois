package io.github.suitougreentea.various_minos_trois.game.magic

import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.game.*
import io.github.suitougreentea.various_minos_trois.rule.MinoColoringStandard
import io.github.suitougreentea.various_minos_trois.rule.MinoRandomizerBag

open class GameMagic(player: Player): BasicMinoGame(player, 10, 50) {
  override fun getRequiredRenderer(app: VariousMinosTrois) = RendererMagic(app, player.playerNumber)

  open var speedMagic = SpeedDataMagic(
          beforeMovingAfterErasing = 10,
          beforeErasingNormal = 0,
          beforeErasingMagic = 0,
          beforeErasingChainNormal = 0,
          erasing = 20
  )

  val minoRandomizer = MinoRandomizerBag(setOf(4, 5, 6, 7, 8, 9, 10))
  val minoRandomizerBlack = MinoRandomizerBag(setOf(0, 1, 3))
  val minoColoring = MinoColoringStandard()

  val minoGenerator = object: MinoGenerator {
    override fun newMino(): Mino {
      if(Math.random() < 0.1) {
        val minoId = minoRandomizerBlack.next()
        val blockPositions = MinoList.list[minoId].second

        val blocks = blockPositions.mapIndexed { i, pos -> Pair(pos, BlockBlack()) }
        return Mino(minoId, blocks)
      } else {
        val minoId = minoRandomizer.next()
        val colorId = minoColoring.getMinoColor(minoId)
        val blockPositions = MinoList.list[minoId].second

        if(Math.random() < 0.2) {
          val blocks = blockPositions.mapIndexed { i, pos -> Pair(pos, BlockRainbow(colorId)) }
          return Mino(minoId, blocks)
        } else {
          val magicColor = (Math.random() * 3).toInt()
          val blocks = blockPositions.mapIndexed { i, pos -> Pair(pos, BlockColored(colorId, magicColor)) }
          return Mino(minoId, blocks)
        }
      }
    }
  }
  override val minoBuffer = MinoBufferInfinite(6, minoGenerator)

  var currentMagicColor = 0
  var currentMagicRotation = 0
  var chain = 0
  var allCascade = false
  var afterErasing = false

  override fun onNewCycle() {
    super.onNewCycle()
    afterErasing = false
    chain = 0
  }

  open inner class StateBeforeMoving: BasicMinoGame.StateBeforeMoving() {
    override val frames = if(afterErasing) speedMagic.beforeMovingAfterErasing else speed.beforeMoving
  }

  open inner class StateAfterMoving: BasicMinoGame.StateAfterMoving() {
    override fun nextState() = if(getLineState().any { it != LineState.NOT_FILLED }) newStateBeforeErasing()
    else newStateBeforeMoving()
  }

  open inner class StateBeforeErasing: State {
    var timer = 0
    var frames = 0

    override fun init() {
      super.init()
      lockRenderTimer = -1
      chain ++
      val lineState = getLineState()
      frames = if(lineState.any { it == LineState.FILLED_RED || it == LineState.FILLED_GREEN || it == LineState.FILLED_BLUE || it == LineState.FILLED_RAINBOW || it == LineState.FILLED_WHITE || it == LineState.FILLED_BLACK }) speedMagic.beforeErasingMagic
      else if(chain >= 2) speedMagic.beforeErasingChainNormal
      else speedMagic.beforeErasingNormal
      if(frames == 0) stateManager.skipState(newStateErasing())
    }
    override fun update() {
      timer ++
      if(timer >= frames) stateManager.changeState(newStateErasing())
    }
  }

  open inner class StateErasing: StateWithTimer() {
    // multiple: all erase, black will turn into color of upper line? (TODO)
    // black: only erase
    // rainbow: erase, black will turn into rainbow, cascade
    override val frames = speedMagic.erasing
    override val stateManager = this@GameMagic.stateManager

    override fun nextState() = if(isCascadeNeeded()) newStateCascade() else newStateBeforeMoving()
    override fun init() {
      val lineState = getLineState()
      lineState.forEachIndexed { i, s ->
        if(s != LineState.NOT_FILLED) {
          field.map.filter { it.key.y == i }.forEach { pos, block ->
            when(block) {
              is BlockColored, is BlockRainbow -> field.remove(pos)
            }
          }
        }
      }
      if(lineState.any { it == LineState.FILLED_RED }) {
        field.map.filter { it.value is BlockColored && (it.value as BlockColored).magicColor == 0 }.forEach { pos, _ -> field.remove(pos) }
        field.map.filter { it.value is BlockBlack }.forEach { pos, _ -> field.set(pos, BlockColored(2, 0))}
      }
      if(lineState.any { it == LineState.FILLED_GREEN }) {
        field.map.filter { it.value is BlockColored && (it.value as BlockColored).magicColor == 1 }.forEach { pos, _ -> field.remove(pos) }
        field.map.filter { it.value is BlockBlack }.forEach { pos, _ -> field.set(pos, BlockColored(14, 1))}
      }
      if(lineState.any { it == LineState.FILLED_BLUE }) {
        field.map.filter { it.value is BlockColored && (it.value as BlockColored).magicColor == 2 }.forEach { pos, _ -> field.remove(pos) }
        field.map.filter { it.value is BlockBlack }.forEach { pos, _ -> field.set(pos, BlockColored(26, 2))}
      }
      if(lineState.any { it == LineState.FILLED_RAINBOW }) {
        field.map.filter { it.value is BlockRainbow }.forEach { pos, _ -> field.remove(pos) }
        field.map.filter { it.value is BlockBlack }.forEach { pos, _ -> field.set(pos, BlockRainbow(2))}
        allCascade = true
      }
      if(lineState.any { it == LineState.FILLED_BLACK }) {
        field.map.filter { it.value is BlockBlack }.forEach { pos, _ -> field.remove(pos) }
      }
      super.init()
    }

    override fun leaveOrSkip() {
      super.leaveOrSkip()
      afterErasing = true
    }
  }

  open inner class StateAfterCascade: BasicMinoGame.StateAfterCascade() {
    override val stateManager = this@GameMagic.stateManager

    override fun nextState() = if(getLineState().any { it != LineState.NOT_FILLED }) newStateBeforeErasing()
    else newStateBeforeMoving()

    override fun leaveOrSkip() {
      super.leaveOrSkip()
      allCascade = false
    }
  }

  override fun newStateBeforeMoving() = StateBeforeMoving()
  override fun newStateAfterMoving() = StateAfterMoving()
  fun newStateBeforeErasing() = StateBeforeErasing()
  fun newStateErasing() = StateErasing()
  override fun newStateAfterCascade() = StateAfterCascade()

  enum class LineState {
    FILLED_NORMAL,
    FILLED_RED,
    FILLED_BLUE,
    FILLED_GREEN,
    FILLED_RAINBOW,
    FILLED_WHITE,
    FILLED_BLACK,
    NOT_FILLED
  }

  fun isCascadeNeeded() = getCurrentCascadeList().filter { it.blocks.all { it.first.y > 0 } }.isNotEmpty()

  fun getLineState() = (0..field.height-1).map { iy ->
    val line = field.map.filter { it.key.y == iy }
    if(line.size == field.width) {
      when {
        line.all { it.value is BlockRainbow } -> LineState.FILLED_RAINBOW
        line.all { it.value.let { (it is BlockColored && it.magicColor == 0) || it is BlockRainbow } } -> LineState.FILLED_RED
        line.all { it.value.let { (it is BlockColored && it.magicColor == 1) || it is BlockRainbow } } -> LineState.FILLED_GREEN
        line.all { it.value.let { (it is BlockColored && it.magicColor == 2) || it is BlockRainbow } } -> LineState.FILLED_BLUE
        line.all { it.value is BlockBlack || it.value is BlockRainbow } -> LineState.FILLED_BLACK
        line.all { it.value is BlockWhite || it.value is BlockRainbow } -> LineState.FILLED_WHITE
        else -> LineState.FILLED_NORMAL
      }
    } else LineState.NOT_FILLED
  }

  override fun spawnNewMino(mino: Mino) {
    super.spawnNewMino(mino)

    val block = mino.blocks[0].second
    currentMagicColor = when(block) {
      is BlockBlack -> -2
      is BlockRainbow -> -1
      is BlockColored -> block.magicColor
      else -> -3
    }
    currentMagicRotation = 0
  }

  override fun attemptToRotateMino(dr: Int): Boolean {
    if (super.attemptToRotateMino(dr)) {
      currentMagicRotation += dr
      if(currentMagicRotation < 0) {
        currentMagicRotation += 4
        if(currentMagicColor != -1) {
          currentMagicColor = (currentMagicColor + 1) % 3
          currentMino?.blocks?.forEach {
            val block = it.second
            if(block is BlockColored) block.magicColor = currentMagicColor
          }
        }
      }
      if(currentMagicRotation >= 4){
        currentMagicRotation -= 4
        if(currentMagicColor != -1) {
          currentMagicColor = (currentMagicColor + 2) % 3
          currentMino?.blocks?.forEach {
            val block = it.second
            if (block is BlockColored) block.magicColor = currentMagicColor
          }
        }
      }
      return true
    } else {
      return false
    }
  }

  override fun getCurrentCascadeList(): MutableList<CascadeData> {
    if(allCascade) {
      val list: MutableList<CascadeData> = ArrayList()

      field.map.forEach { p, b ->
        list.add(CascadeData(listOf(Pair(p, b))))
      }

      list.sortBy { it.blocks.minBy { it.first.y }?.first?.y ?: 0 }

      return list
    } else return super.getCurrentCascadeList()
  }

  open class BlockBase: Block {
    override var fixed = false
  }
  class BlockColored(var color: Int, var magicColor: Int): BlockBase()
  class BlockRainbow(var color: Int): BlockBase()
  class BlockWhite(var level: Int): BlockBase()
  class BlockBlack(): BlockBase()

  data class SpeedDataMagic(
          var beforeMovingAfterErasing: Int,
          var beforeErasingNormal: Int,
          var beforeErasingChainNormal: Int,
          var beforeErasingMagic: Int,
          var erasing: Int
  )
}