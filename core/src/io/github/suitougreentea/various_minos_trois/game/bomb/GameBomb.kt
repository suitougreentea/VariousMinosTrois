package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.game.*
import io.github.suitougreentea.various_minos_trois.game.MinoBufferInfinite
import io.github.suitougreentea.various_minos_trois.rule.MinoColoringStandard
import io.github.suitougreentea.various_minos_trois.rule.MinoRandomizerBag
import java.util.*

// TODO: フリーズ状態をBlockに含める
open class GameBomb(input: Input): BasicMinoGame(input, 10, 50) {
  val bombSize = arrayOf (Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3), Pair(4, 4), Pair(4, 4), Pair(5, 5), Pair(5, 5), Pair(6, 6), Pair(6, 6), Pair(7, 7), Pair(7, 7), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8))

  val minoRandomizer = MinoRandomizerBag(setOf(4, 5, 6, 7, 8, 9, 10))
  val minoColoring = MinoColoringStandard()
  val minoGenerator = object: MinoGenerator {
    var count = 0
    override fun newMino(): Mino {
      val minoId = minoRandomizer.next()
      val colorId = minoColoring.getMinoColor(minoId)
      val blockPositions = MinoList.list[minoId].second
      val bombIndex = (Math.random() * blockPositions.size).toInt()

      val blocks = if((count + allBombOffset) % allBombFrequency == 0) {
        blockPositions.mapIndexed { _, pos -> Pair(pos, BlockBomb()) }
      } else if((count + bombOffset) % bombFrequency == 0) {
        blockPositions.mapIndexed { i, pos ->
          when(i) {
            bombIndex -> Pair(pos, BlockBomb())
            else -> Pair(pos, BlockNormal(colorId))
          }
        }
      } else {
        blockPositions.mapIndexed { _, pos -> Pair(pos, BlockNormal(colorId)) }
      }

      count++

      return Mino(minoId, blocks)
    }
  }

  var bombFrequency = 2
  var bombOffset = 0
  var allBombFrequency = 40
  var allBombOffset = 1

  override val minoBuffer = MinoBufferInfinite(6, minoGenerator)

  open var speedBomb = SpeedDataBomb(
          count = 10,
          beforeExplosion = 10,
          explosion = 10,
          afterExplosion = 5,
          bigBomb = 10
  )

  var countTimer = 0
  var explosionTimer = 0
  var bigBombTimer = 0

  var explosionList: MutableList<ExplosionData> = ArrayList()
  var bigBombList: MutableList<Pos> = ArrayList()

  var countLines: MutableList<Int> = ArrayList()
  var countLinesIndex = 0
  var freezedLines: MutableList<Int> = ArrayList()
  var chain = 0
  var currentExplosionSize = 0

  var bombedBlocks = 0

  var countNumber = 0

  enum class LineState {
    FILLED_WITHOUT_BOMB,
    FILLED_WITH_BOMB,
    NOT_FILLED
  }

  fun getLineState() = (0..field.height-1).map { iy ->
    val line = field.map.filter { it.key.y == iy }
    if(line.size == field.width) {
      if(line.any { it.value is BlockBomb || it.value is BlockBigBomb }) LineState.FILLED_WITH_BOMB else LineState.FILLED_WITHOUT_BOMB
    } else LineState.NOT_FILLED
  }

  fun isAnyLineFilled() = getLineState().any { it == LineState.FILLED_WITHOUT_BOMB || it == LineState.FILLED_WITH_BOMB }
  fun isExplodable() = getLineState().any { it == LineState.FILLED_WITH_BOMB }

  fun isBigBombNeeded() = field.map.filterValues { it is BlockBomb }.keys.any {
    arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1)).all {
      field.contains(it) && field.get(it) is BlockBomb
    }
  }

  fun isCascadeNeeded() = getCurrentCascadeList().filter { it.blocks.all { it.first.y > 0 && !it.second.fixed } }.isNotEmpty()

  open inner class StateBeforeMoving: BasicMinoGame.StateBeforeMoving() {
    override fun enter() {
      super.enter()

      freezedLines.clear()
      getLineState().forEachIndexed { i, e -> if (e == LineState.FILLED_WITHOUT_BOMB) freezedLines.add(i) }
    }
  }

  override fun newStateBeforeMoving() = StateBeforeMoving()
  override fun newStateMoving() = StateMoving()
  override fun newStateAfterMoving() = StateAfterMoving()
  open fun newStateCounting() = StateCounting()
  open fun newStateBeforeExplosion() = StateBeforeExplosion()
  open fun newStateExplosion() = StateExplosion()
  open fun newStateAfterExplosion() = StateAfterExplosion()
  override fun newStateCascade() = StateCascade()
  override fun newStateAfterCascade() = StateAfterCascade()
  open fun newStateMakingBigBomb() = StateMakingBigBomb()

  override fun newCycle() {
    super.newCycle()
    chain = 0
    bombedBlocks = 0
  }

  open inner class StateAfterMoving: BasicMinoGame.StateAfterMoving() {
    override fun nextState() = if(getLineState().filter { it == GameBomb.LineState.FILLED_WITHOUT_BOMB || it == GameBomb.LineState.FILLED_WITH_BOMB }.size > freezedLines.size) newStateCounting()
    else if(isCascadeNeeded()) newStateCascade()
    else if(isBigBombNeeded()) newStateMakingBigBomb()
    else newStateBeforeMoving()
  }

  open inner class StateCounting(): State {
    fun nextState() = if (isExplodable()) newStateBeforeExplosion()
    else if (isCascadeNeeded()) newStateCascade()
    else if (isBigBombNeeded()) newStateMakingBigBomb()
    else newStateBeforeMoving()

    override fun enter() {
      countNumber = 0
      chain ++
      getLineState().forEachIndexed { i, e -> if(e == LineState.FILLED_WITHOUT_BOMB || e == LineState.FILLED_WITH_BOMB) countLines.add(i) }
    }

    override fun update() {
      if(speedBomb.count == 0) {
        seQueue.add("count")
        countNumber = chain + countLines.size - 2
        stateManager.changeState(nextState())
      } else {
        if (countTimer == 0) {
          seQueue.add("count")
          countNumber = chain + countLinesIndex - 1
        }
        if (countTimer == speedBomb.count) {
          if (countLinesIndex == countLines.size - 1) {
            stateManager.changeState(nextState())
          } else {
            countLinesIndex++
            countTimer = 0
          }
        } else countTimer++
      }
      if(lockRenderTimer >= 0) lockRenderTimer ++
      acceptMoveInput()
    }

    override fun leave() {
      super.leave()
      countLines.clear()
      countLinesIndex = 0
      countTimer = 0
    }
  }

  open inner class StateBeforeExplosion: StateWithTimer() {
    override val frames = speedBomb.beforeExplosion
    override val stateManager = this@GameBomb.stateManager

    override fun nextState() = newStateExplosion()
    override fun enter() {
      super.enter()
      getLineState().forEachIndexed { i, e -> if(e == LineState.FILLED_WITH_BOMB) {
        field.map.filter { it.key.y == i }.forEach {
          val (pos, block) = it
          if(block is BlockBomb) block.ignited = true
          if(block is BlockBigBomb) {
            block.ignited = true
            when (block.position){
              BlockBigBomb.Position.BOTTOM_LEFT, BlockBigBomb.Position.BOTTOM_RIGHT -> (field.get(Pos(pos.x, pos.y + 1)) as BlockBigBomb).ignited = true
              BlockBigBomb.Position.TOP_LEFT, BlockBigBomb.Position.TOP_RIGHT -> (field.get(Pos(pos.x, pos.y - 1)) as BlockBigBomb).ignited = true
            }
          }
        }
      } }
      currentExplosionSize = chain + getLineState().filter { it == LineState.FILLED_WITHOUT_BOMB || it == LineState.FILLED_WITH_BOMB }.size - 2
    }
    override fun update() {
      super.update()
      if(lockRenderTimer >= 0) lockRenderTimer ++
      acceptMoveInput()
    }
  }

  open inner class StateExplosion(): State {
    override fun enter() {
      super.enter()
      lockRenderTimer = -1
    }
    override fun update() {
      if(explosionTimer == 0) {
        if(field.map.values.any { it is BlockBomb && it.ignited }) seQueue.add("explosion_small")
        if(field.map.values.any { it is BlockBigBomb && it.ignited }) seQueue.add("explosion_big")
        field.map.filterValues { it is BlockBomb && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, currentExplosionSize)) }
        field.map.filterValues { it is BlockBigBomb && it.position == BlockBigBomb.Position.BOTTOM_LEFT && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, -1)) }
      }
      if(explosionTimer == speedBomb.explosion * 1/3) {
        val explosionPositions: MutableSet<Pos> = HashSet()
        explosionList.forEach {
          val (cx, cy) = it.position
          val (rx, ry) = if(it.size == -1) {
            Pair((cx - 4 .. cx + 5), (cy - 4 .. cy + 5))
          } else {
            val (w, h) = bombSize.get(it.size)
            Pair((cx - w .. cx + w), (cy - h .. cy + h))
          }
          field.remove(Pos(cx, cy))
          bombedBlocks ++
          if(it.size == -1) {
            field.remove(Pos(cx + 1, cy))
            field.remove(Pos(cx, cy + 1))
            field.remove(Pos(cx + 1, cy + 1))
            bombedBlocks += 3
          }
          field.map.keys.filter { rx.contains(it.x) && ry.contains(it.y) }.forEach { explosionPositions.add(it) }
        }

        explosionPositions.forEach {
          val block = field.get(it)
          when(block) {
            is BlockNormal -> { field.remove(it); bombedBlocks ++ }
            is BlockBomb -> block.ignited = true
            is BlockBigBomb -> when(block.position) {
              BlockBigBomb.Position.BOTTOM_LEFT -> arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y + 1), Pos(it.x + 1, it.y + 1))
              BlockBigBomb.Position.BOTTOM_RIGHT -> arrayOf(Pos(it.x - 1, it.y), Pos(it.x, it.y), Pos(it.x - 1, it.y + 1), Pos(it.x, it.y + 1))
              BlockBigBomb.Position.TOP_LEFT -> arrayOf(Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1), Pos(it.x, it.y), Pos(it.x + 1, it.y))
              BlockBigBomb.Position.TOP_RIGHT -> arrayOf(Pos(it.x - 1, it.y - 1), Pos(it.x, it.y - 1), Pos(it.x - 1, it.y), Pos(it.x, it.y))
            }.forEach { (field[it] as BlockBigBomb).ignited = true }
            is BlockWhite -> if(block.level == 0) { field.remove(it); bombedBlocks ++ } else block.level --
            is BlockBlack -> if(block.level == 0) { field.remove(it); bombedBlocks ++ } else block.level --
          }
        }
      }
      if(explosionTimer == speedBomb.explosion) {
        explosionTimer = 0
        explosionList = ArrayList()
        if(!field.map.any { val block = it.value; (block is BlockBomb && block.ignited) || (block is BlockBigBomb && block.ignited) }) stateManager.changeState(newStateAfterExplosion())
      } else explosionTimer ++
      acceptMoveInput()
    }
  }

  open inner class StateAfterExplosion: StateWithTimer() {
    override val frames = speedBomb.afterExplosion
    override val stateManager = this@GameBomb.stateManager

    override fun nextState() = if(isCascadeNeeded()) newStateCascade()
    else if(isBigBombNeeded()) newStateMakingBigBomb()
    else newStateBeforeMoving()

    override fun update() {
      super.update()
      acceptMoveInput()
    }
  }

  open inner class StateAfterCascade: BasicMinoGame.StateAfterCascade() {
    override val stateManager = this@GameBomb.stateManager

    override fun nextState() = if(isExplodable()) newStateCounting()
    else if(isBigBombNeeded()) newStateMakingBigBomb()
    else newStateBeforeMoving()

    override fun update() {
      super.update()
      acceptMoveInput()
    }
  }

  open inner class StateMakingBigBomb: State {
    override fun enter() {
      super.enter()
      val bigBombReservedList: MutableList<Pos> = ArrayList()
      // Sort: (0, 10), (1, 10), (0, 9), (1, 9) ...
      field.map.filterValues { it is BlockBomb }.keys.sortedWith(Comparator { a, b -> if (a.y == b.y) a.x - b.x else b.y - a.y }).forEach {
        val blockPositions = arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1))
        if(blockPositions.all { field.contains(it) && field.get(it) is BlockBomb && !bigBombReservedList.contains(it) }) {
          bigBombList.add(Pos(it.x, it.y - 1))
          blockPositions.forEach { bigBombReservedList.add(it) }
        }
      }
      bigBombReservedList.forEach { field.remove(it) }
      bigBombTimer = 0
      seQueue.add("big_bomb")
    }

    override fun update() {
      if(bigBombTimer == speedBomb.bigBomb) {
        bigBombList.forEach {
          field[Pos(it.x, it.y)] = BlockBigBomb(BlockBigBomb.Position.BOTTOM_LEFT)
          field[Pos(it.x + 1, it.y)] = BlockBigBomb(BlockBigBomb.Position.BOTTOM_RIGHT)
          field[Pos(it.x, it.y + 1)] = BlockBigBomb(BlockBigBomb.Position.TOP_LEFT)
          field[Pos(it.x + 1, it.y + 1)] = BlockBigBomb(BlockBigBomb.Position.TOP_RIGHT)
        }
        bigBombList = ArrayList()
        stateManager.changeState(newStateBeforeMoving())
      } else bigBombTimer ++
      acceptMoveInput()
    }

    override fun leave() {
      super.leave()
    }
  }

  // BigBomb is size=-1
  class ExplosionData(val position: Pos, val size: Int) {}

  open class BlockBase: Block {
    override var fixed = false
  }
  class BlockNormal(val color: Int): BlockBase()

  class BlockBomb: BlockBase() {
    var ignited = false
  }

  class BlockBigBomb(val position: Position): BlockBase() {
    enum class Position {
      BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT
    }
    var ignited = false
  }

  class BlockWhite(var level: Int): BlockBase()

  class BlockWhiteUnbreakable(): BlockBase()

  class BlockBlack(var level: Int): BlockBase() {
    override var fixed = true
  }

  class BlockBlackUnbreakable(): BlockBase() {
    override var fixed = true
  }

  data class SpeedDataBomb(
          var count: Int,
          var beforeExplosion: Int,
          var explosion: Int,
          var afterExplosion: Int,
          var bigBomb: Int
  )
}
