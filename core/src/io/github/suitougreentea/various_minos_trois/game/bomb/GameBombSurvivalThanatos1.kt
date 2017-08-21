package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.Input
import io.github.suitougreentea.various_minos_trois.Player
import io.github.suitougreentea.various_minos_trois.rule.Rule

class GameBombSurvivalThanatos1(player: Player, rule: Rule): GameBombSurvival(player, rule) {
  val allBombFrequencyList = listOf(
          Pair(  0, 50)
  )

  // beforeMoving, beforeMovingAfterExplosion, moveStart, lock, beforeExplosion, explosion, afterExplosion, bigbomb
  val otherSpeed = listOf(
          Pair(  0, listOf(10, 6, 8, 18, 1, 5, 2, 4)),
          Pair(100, listOf(10, 5, 6, 18, 1, 4, 2, 4)),
          Pair(200, listOf(10, 4, 6, 17, 1, 3, 2, 4)),
          Pair(300, listOf( 4, 4, 6, 15, 0, 3, 0, 0)),
          Pair(400, listOf( 4, 3, 4, 13, 0, 2, 0, 0)),
          Pair(500, listOf( 4, 3, 4, 12, 0, 2, 0, 0)),
          Pair(600, listOf( 4, 3, 4, 10, 0, 2, 0, 0)),
          Pair(700, listOf( 4, 3, 4,  8, 0, 2, 0, 0)),
          Pair(800, listOf( 4, 3, 4,  7, 0, 2, 0, 0)),
          Pair(900, listOf( 4, 3, 4,  6, 0, 2, 0, 0))
  )

  override val speedUpdateFunctionList = listOf(
          generateSpeedUpdateFunction(this::allBombFrequency, allBombFrequencyList),
          generateSpeedUpdateFunction(
                  listOf(speed::beforeMoving, speedBomb::beforeMovingAfterExplosion, speed::moveStart, speed::lock, speedBomb::beforeExplosion, speedBomb::explosion, speedBomb::afterExplosion, speedBomb::bigBomb),
                  otherSpeed
          ),
          generateSpeedUpdateFunction({ e -> speedBomb.beforeMovingAfterFreezeLineCount = e }, otherSpeed, 0),
          generateSpeedUpdateFunction({ e -> speed.forceLock = e * 3 }, otherSpeed, 3)
  )
}
