package io.github.suitougreentea.various_minos_trois.game.bomb

import com.badlogic.gdx.math.MathUtils
import io.github.suitougreentea.various_minos_trois.Input
import io.github.suitougreentea.various_minos_trois.Player
import java.io.File
import kotlin.comparisons.maxOf
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty0

open class GameBombSurvival(player: Player): GameBomb(player) {
  var level = 99

  var lines = 0

  var score = 0

  open val clearLevel = 999

  override var speed = SpeedDataBasicMino(
          beforeMoving = 30,
          moveStart = 14,
          drop = 22f,
          moveSpeed = 1f,
          softDrop = 1f,
          lock = 30,
          forceLock = 90,
          afterMoving = 0,
          cascade = 30f,
          afterCascade = 0
  )

  override var speedBomb = SpeedDataBomb(
          count = 0,
          beforeExplosion = 8,
          explosion = 15,
          afterExplosion = 10,
          bigBomb = 8
  )

  open val speedUpdateFunctionList: List<(Int) -> Unit> = listOf()

  override fun onNewCycle() {
    super.onNewCycle()
    if(level < clearLevel) {
      addLevel(1, true)
    } else {
      val src = File("log.txt").absoluteFile
      src.writeText(log.joinToString("\n"))
    }
  }

  override fun newStateAfterExplosion() = StateAfterExplosion()
  override fun newStateMakingBigBomb() = StateMakingBigBomb()

  override fun onLineFilled() {
    val lines = getLineState().filter { it == GameBomb.LineState.FILLED_WITHOUT_BOMB || it == GameBomb.LineState.FILLED_WITH_BOMB }.size
    val linesWithBomb = getLineState().filter { it == GameBomb.LineState.FILLED_WITH_BOMB }.size
    val basePoint = chain * 2 + lines - 2

    if(linesWithBomb == 0) {
      addLevel((basePoint * 0.5f).toInt(), true)
      log.add("${level},${gameTimer},${lines},${chain},${droppedBlocks},0,0")
      addScore(getLineScore(level, lines, chain, droppedBlocks, 0))
    } else {
      addLevel(basePoint, false)
    }
    this@GameBombSurvival.lines = lines
    this@GameBombSurvival.chain = chain
  }

  open inner class StateAfterExplosion: GameBomb.StateAfterExplosion() {
    override fun leave() {
      super.leave()
      addLevel((bombedBlocks * 0.04f).toInt(), true)
      addScore(getLineScore(level, lines, chain, droppedBlocks, bombedBlocks))
      log.add("${level},${gameTimer},${lines},${chain},${droppedBlocks},${bombedBlocks},0")
    }
  }

  open inner class StateMakingBigBomb: GameBomb.StateMakingBigBomb() {
    override fun enter() {
      super.enter()
      val num = bigBombList.size
      addLevel(num * 2, true)
      addScore(getBigBombScore(level, num))
      log.add("${level},${gameTimer},0,0,0,0,${num}")
    }
  }

  fun addLevel(num: Int, freezeSection: Boolean) {
    if(level == 999) return
    val newLevel = level + num
    if(newLevel >= clearLevel) {
      if(freezeSection) {
        level = clearLevel - 1
      } else {
        level = clearLevel

        onGameCleared()
        enableTimer = false
      }
    } else if(freezeSection && newLevel / 100 != level / 100) {
      level = level / 100 * 100 + 99
    } else {
      level = newLevel
    }
    val oldBackground = background
    background = level / 100
    if(background != oldBackground) player.updateBackground(background, true)

    speedUpdateFunctionList.forEach { it.invoke(level) }
  }

  open fun onGameCleared() {}

  open fun addScore(score: Int) {
    this.score += score
  }

  open fun getLevelMultiplier(level: Int) = 276f * MathUtils.log(10f, 0.000167f * Math.pow(level.toDouble(), 1.8).toFloat() + 1.3f) - 27f
  open fun getLineBase(line: Int, chain: Int, freeze: Boolean): Float {
    if(freeze) return MathUtils.log(10f, line + chain - 1f) + 0.5f
    val linePointA = (1f / (1f + 0.25f * (chain - 1f))) * line + (chain * 2f - 3f)
    val linePointB = line + chain - 1f
    val linePoint = maxOf(linePointA, linePointB)
    return if(line <= 4) linePoint * linePoint + chain * 2f - 2f
    else 4f * linePoint + 16f * MathUtils.log(10f, linePoint - 2f) - 5f + chain * 2f - 2f
  }
  open fun getLineScore(level: Int, line: Int, chain: Int, drop: Int, block: Int) = ((getLineBase(line, chain, block == 0) + block / 20f) * (drop / 50f + 1f) * getLevelMultiplier(level)).toInt()
  open fun getBigBombScore(level: Int, big: Int) = (3.5f * big * big * getLevelMultiplier(level)).toInt()

  fun <T> generateSpeedUpdateFunction(ref: KMutableProperty0<T>, list: List<Pair<Int, T>>): (Int) -> Unit = { level ->
    val e = list.last { it.first <= level }
    ref.set(e.second)
  }

  fun <T> generateSpeedUpdateFunction(refList: List<KMutableProperty0<T>>, list: List<Pair<Int, List<T>>>): (Int) -> Unit = { level ->
    val e = list.last { it.first <= level }
    refList.forEachIndexed { i, ref -> ref.set(e.second[i]) }
  }
}