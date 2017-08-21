package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.Input
import com.badlogic.gdx.math.MathUtils
import io.github.suitougreentea.various_minos_trois.Player
import io.github.suitougreentea.various_minos_trois.rule.Rule
import kotlin.comparisons.maxOf

class GameBombSurvivalMaster1(player: Player, rule: Rule): GameBombSurvival(player, rule) {
  val grades = listOf(
          Pair( 1,      0),
          Pair( 2,    200),
          Pair( 3,    800),
          Pair( 4,   1600),
          Pair( 5,   3200),
          Pair( 6,   6400),
          Pair( 7,  10000),
          Pair( 8,  14000),
          Pair( 9,  18000),
          Pair(10,  23000),
          Pair(35,  28000),
          Pair(36,  33000),
          Pair(37,  38000),
          Pair(38,  44000),
          Pair(39,  50000),
          Pair(40,  56000),
          Pair(41,  63000),
          Pair(42,  70000),
          Pair(43,  76000),
          Pair(44,  82000),
          Pair(45,  88000),
          Pair(46,  94000),
          Pair(47, 100000),
          Pair(48, 106000),
          Pair(49, 113000),
          Pair(50, 120000),
          Pair(51, 128000),
          Pair(52, 136000),
          Pair(53, 145000),
          Pair(54, 154000),
          Pair(55, 164000),
          Pair(56, 175000),
          Pair(57, 187000),
          Pair(58, 200000),
          Pair(59, -1)
  )

  var gradeIndex = 0
  var nextScore = grades[1].second

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
  // beforeMoving, beforeMovingAfterFreezeLineCount/Explosion, lock, beforeExplosion, explosion, afterExplosion, bigbomb
  val otherSpeed = listOf(
          Pair(  0, listOf(28, 25, 30, 8, 15, 10, 8)),
          Pair(600, listOf(25, 20, 30, 6, 12, 8, 8)),
          Pair(700, listOf(20, 15, 30, 6, 12, 8, 8)),
          Pair(800, listOf(20, 12, 30, 4, 10, 6, 6)),
          Pair(900, listOf(16,  8, 20, 4, 10, 6, 6))
  )

  override val speedUpdateFunctionList = listOf(
    generateSpeedUpdateFunction(speed::drop, dropSpeed),
    generateSpeedUpdateFunction(this::allBombFrequency, allBombFrequencyList),
    generateSpeedUpdateFunction(
          listOf(speed::beforeMoving, speedBomb::beforeMovingAfterExplosion, speed::lock, speedBomb::beforeExplosion, speedBomb::explosion, speedBomb::afterExplosion, speedBomb::bigBomb),
          otherSpeed
    ),
    generateSpeedUpdateFunction({ e -> speedBomb.beforeMovingAfterFreezeLineCount = e }, otherSpeed, 0),
    generateSpeedUpdateFunction({ e -> speed.forceLock = e * 3 }, otherSpeed, 2)
  )

  override fun onGameCleared() {
    if(gradeIndex == 33 && gameTimer < 32400) {
      gradeIndex = 34
    }
  }

  override fun addScore(score: Int) {
    super.addScore(score)
    if(gradeIndex <= 32) {
      val newGradeIndex = grades.dropLast(1).indexOfLast { it.second <= this.score }
      if(gradeIndex < newGradeIndex) {
        gradeIndex = newGradeIndex
        nextScore = grades[gradeIndex + 1].second
      }
    }
  }
}