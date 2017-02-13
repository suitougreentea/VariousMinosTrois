package io.github.suitougreentea.various_minos_trois.game.bomb

import com.badlogic.gdx.math.MathUtils
import io.github.suitougreentea.various_minos_trois.game.State
import io.github.suitougreentea.various_minos_trois.Input
import java.io.File
import kotlin.comparisons.maxOf

class GameBombSurvival(input: Input): GameBomb(input) {
  var level = 900
  var nextLevel = 100

  var lines = 0
  var log = mutableListOf<String>()

  var score = 0

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

  val dropSpeed = listOf(
          Pair(  0, 1/60f),
          Pair( 20, 1/45f),
          Pair( 40, 1/30f),
          Pair( 55, 1/25f),
          Pair( 70, 1/20f),
          Pair( 85, 1/15f),
          Pair(100, 1/10f),
          Pair(120, 1/8f),
          Pair(140, 1/6f),
          Pair(150, 1/5f),
          Pair(170, 1/3f),
          Pair(190, 1/2f),
          Pair(200, 1/30f),
          Pair(220, 1/20f),
          Pair(240, 1/10f),
          Pair(260, 1/5f),
          Pair(280, 1/2f),
          Pair(300, 1f),
          Pair(350, 2f),
          Pair(400, 3f),
          Pair(425, 4f),
          Pair(450, 5f),
          Pair(460, 4f),
          Pair(470, 3f),
          Pair(480, 2f),
          Pair(490, 1f),
          Pair(500, 30f)
  )

  val allBombFrequencyList = listOf(
          Pair(  0, 20),
          Pair(100, 20),
          Pair(200, 20),
          Pair(300, 18),
          Pair(400, 16),
          Pair(500, 18),
          Pair(600, 20),
          Pair(700, 25),
          Pair(800, 30),
          Pair(900, 35)
  )
  // beforeMoving, lock, beforeExplosion, explosion, afterExplosion, bigbomb
  val otherSpeed = listOf(
          arrayOf(  0, 30, 30, 8, 15, 10, 8),
          arrayOf(600, 25, 30, 6, 12, 8, 8),
          arrayOf(700, 20, 30, 6, 12, 8, 8),
          arrayOf(800, 20, 30, 4, 10, 6, 6),
          arrayOf(900, 16, 20, 4, 10, 6, 6)
  )

  override fun newCycle() {
    super.newCycle()
    if(level < 999) {
      addLevel(1, true)
    } else {
      val src = File("log.txt").absoluteFile
      src.writeText(log.joinToString("\n"))
    }
  }

  override fun newStateCounting() = StateCounting()
  override fun newStateAfterExplosion() = StateAfterExplosion()
  override fun newStateMakingBigBomb() = StateMakingBigBomb()

  open inner class StateCounting: GameBomb.StateCounting() {
    override fun leave() {
      super.leave()
      val lines = getLineState().filter { it == GameBomb.LineState.FILLED_WITHOUT_BOMB || it == GameBomb.LineState.FILLED_WITH_BOMB }.size
      val linesWithBomb = getLineState().filter { it == GameBomb.LineState.FILLED_WITH_BOMB }.size
      val basePoint = chain * 2 + lines - 2

      if(linesWithBomb == 0) {
        addLevel((basePoint * 0.5f).toInt(), true)
        log.add("${level},${gameTimer},${lines},${chain},${drop},0,0")
        score += getLineScore(level, lines, chain, drop, 0)
      } else {
        addLevel(basePoint, false)
      }
      this@GameBombSurvival.lines = lines
      this@GameBombSurvival.chain = chain
    }
  }

  open inner class StateAfterExplosion: GameBomb.StateAfterExplosion() {
    override fun leave() {
      super.leave()
      addLevel((bombedBlocks * 0.04f).toInt(), true)
      score += getLineScore(level, lines, chain, drop, bombedBlocks)
      log.add("${level},${gameTimer},${lines},${chain},${drop},${bombedBlocks},0")
    }
  }

  open inner class StateMakingBigBomb: GameBomb.StateMakingBigBomb() {
    override fun enter() {
      super.enter()
      val num = bigBombList.size
      addLevel(num * 2, true)
      score += getBigBombScore(level, num)
      log.add("${level},${gameTimer},0,0,0,0,${num}")
    }
  }

  fun addLevel(num: Int, freezeSection: Boolean) {
    if(level == 999) return
    val newLevel = level + num
    if(newLevel >= 999) {
      if(freezeSection) {
        level = 998
      } else {
        level = 999
      }
    } else if(freezeSection && newLevel / 100 != level / 100) {
      level = level / 100 * 100 + 99
    } else {
      level = newLevel
    }
    background = level / 100

    val e = dropSpeed.last { it.first <= level }
    speed.drop = e.second

    val f = otherSpeed.last { it[0] <= level }
    speed.beforeMoving = f[1]
    speed.lock = f[2]
    speedBomb.beforeExplosion = f[3]
    speedBomb.explosion = f[4]
    speedBomb.afterExplosion = f[5]
    speedBomb.bigBomb = f[6]

    val g = allBombFrequencyList.last { it.first <= level }
    allBombFrequency = g.second
  }

  open inner class LogOutputState(): State {
    override fun enter() {
      super.enter()
    }
    override fun update() {
    }
  }

  fun newLogOutputState() = LogOutputState()

  fun getLevelMultiplier(level: Int) = 276f * MathUtils.log(10f, 0.000167f * Math.pow(level.toDouble(), 1.8).toFloat() + 1.3f) - 27f
  fun getLineBase(line: Int, chain: Int, freeze: Boolean): Float {
    if(freeze) return MathUtils.log(10f, line.toFloat()) + 0.5f
    val linePointA = (1f / (1f + 0.25f * (chain - 1f))) * line + (chain * 2f - 3f)
    val linePointB = line + chain - 1f
    val linePoint = maxOf(linePointA, linePointB)
    return if(line <= 4) linePoint * linePoint + chain * 2f - 2f
    else 4f * linePoint + 16f * MathUtils.log(10f, linePoint - 2f) - 5f + chain * 2f - 2f
  }

  fun getLineScore(level: Int, line: Int, chain: Int, drop: Int, block: Int) = ((getLineBase(line, chain, block == 0) + block / 20f) * (drop / 50f + 1f) * getLevelMultiplier(level)).toInt()
  fun getBigBombScore(level: Int, big: Int) = (3.5f * big * big * getLevelMultiplier(level)).toInt()
}