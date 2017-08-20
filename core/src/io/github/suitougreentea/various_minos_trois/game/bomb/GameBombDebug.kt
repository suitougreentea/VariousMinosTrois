package io.github.suitougreentea.various_minos_trois.game.bomb

import io.github.suitougreentea.various_minos_trois.Player

open class GameBombDebug(player: Player): GameBomb(player) {
  override var speed = SpeedDataBasicMino(
      beforeMoving = 30,
      moveStart = 10,
      moveSpeed = 1f,
      drop = 1 / 60f,
      softDrop = 1f,
      lock = 60,
      forceLock = 180,
      afterMoving = 0,
      cascade = 22f,
      afterCascade = 0
  )

  override var speedBomb = SpeedDataBomb(
      beforeMovingAfterFreezeLineCount = 15,
      beforeMovingAfterExplosion = 0,
      count = 10,
      beforeExplosion = 10,
      explosion = 20,
      afterExplosion = 10,
      bigBomb = 10
  )
}
