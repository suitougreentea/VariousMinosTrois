package io.github.suitougreentea.various_minos_trois.game.magic

import io.github.suitougreentea.various_minos_trois.Player

class GameMagicDebug(player: Player): GameMagic(player) {
  override var speed = SpeedDataBasicMino(
      beforeMoving = 10,
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

  override var speedMagic = SpeedDataMagic(
      beforeErasingNormal = 0,
      beforeErasingMagic = 0,
      beforeErasingChainNormal = 0,
      erasing = 20
  )
}