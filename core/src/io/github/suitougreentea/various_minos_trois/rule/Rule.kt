package io.github.suitougreentea.various_minos_trois.rule

interface Rule {
  fun getMinoRandomizer(): MinoRandomizer
  fun getRotationSystem(): RotationSystem
  fun getMinoColoring(): MinoColoring
  fun getSpawnSystem(width: Int, viewHeight: Int): SpawnSystem
  val moveAfterRotation: Boolean
  val disableUpKey: Boolean
  val upKeyLock: Boolean
  val alternativeLock: Boolean
  val multiNextAndHold: Boolean
  val initialRotation: Boolean
}

class RuleModern: Rule {
  override fun getMinoRandomizer() = MinoRandomizerBag()
  override fun getRotationSystem() = RotationSystemStandard()
  override fun getMinoColoring() = MinoColoringStandard()
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemStandard(width, viewHeight)
  override val moveAfterRotation = false
  override val disableUpKey = false
  override val upKeyLock = true
  override val alternativeLock = false
  override val multiNextAndHold = true
  override val initialRotation = true
}

class RuleVariant: Rule {
  override fun getMinoRandomizer() = MinoRandomizerBag()
  override fun getRotationSystem() = RotationSystemVariant(false)
  override fun getMinoColoring() = MinoColoringVariant()
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemVariant(width, viewHeight)
  override val moveAfterRotation = true
  override val disableUpKey = false
  override val upKeyLock = false
  override val alternativeLock = false
  override val multiNextAndHold = true
  override val initialRotation = true
}

class RuleClassic: Rule {
  override fun getMinoRandomizer() = MinoRandomizerBag()
  override fun getRotationSystem() = RotationSystemVariant(true)
  override fun getMinoColoring() = MinoColoringVariant()
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemVariant(width, viewHeight)
  override val moveAfterRotation = true
  override val disableUpKey = false
  override val upKeyLock = false
  override val alternativeLock = false
  override val multiNextAndHold = false
  override val initialRotation = true
}

class RuleRetro: Rule {
  override fun getMinoRandomizer() = MinoRandomizerRandom()
  override fun getRotationSystem() = RotationSystemDefault()
  override fun getMinoColoring() = MinoColoringRetro(20)
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemVariant(width, viewHeight)
  override val moveAfterRotation = true
  override val disableUpKey = true
  override val upKeyLock = false
  override val alternativeLock = true
  override val multiNextAndHold = false
  override val initialRotation = false
}
