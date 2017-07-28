package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.Input

class GameBombSurvivalThanatos1(input: Input): GameBombSurvival(input) {
  val allBombFrequencyList = listOf(
          Pair(  0, 50)
  )

  // beforeMoving, moveStart, lock, forceLock, beforeExplosion, explosion, afterExplosion, bigbomb
  val otherSpeed = listOf(
          Pair(  0, listOf(10, 8, 18, 18*3, 1, 5, 2, 4)),
          Pair(100, listOf(10, 6, 18, 18*3, 1, 4, 2, 4)),
          Pair(200, listOf(10, 6, 17, 17*3, 1, 3, 2, 4)),
          Pair(300, listOf( 4, 6, 15, 15*3, 0, 3, 0, 0)),
          Pair(400, listOf( 4, 4, 13, 13*3, 0, 2, 0, 0)),
          Pair(500, listOf( 4, 4, 12, 12*3, 0, 2, 0, 0)),
          Pair(600, listOf( 4, 4, 10, 10*3, 0, 2, 0, 0)),
          Pair(700, listOf( 4, 4,  8,  8*3, 0, 2, 0, 0)),
          Pair(800, listOf( 4, 4,  7,  7*3, 0, 2, 0, 0)),
          Pair(900, listOf( 4, 4,  6,  6*3, 0, 2, 0, 0))
  )

  override val speedUpdateFunctionList = listOf(
          generateSpeedUpdateFunction(this::allBombFrequency, allBombFrequencyList),
          generateSpeedUpdateFunction(
                  listOf(speed::beforeMoving, speed::moveStart, speed::lock, speed::forceLock, speedBomb::beforeExplosion, speedBomb::explosion, speedBomb::afterExplosion, speedBomb::bigBomb),
                  otherSpeed
          )
  )
}
