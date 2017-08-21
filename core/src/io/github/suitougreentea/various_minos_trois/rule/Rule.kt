package io.github.suitougreentea.various_minos_trois.rule

interface Rule {
  fun getMinoRandomizer(): MinoRandomizer
  fun getRotationSystem(): RotationSystem
  fun getMinoColoring(): MinoColoring
  fun getSpawnSystem(width: Int, viewHeight: Int): SpawnSystem
  val moveAfterRotation: Boolean
  val upKeyNoLock: Boolean
}

class RuleModern: Rule {
  override fun getMinoRandomizer() = MinoRandomizerBag()
  override fun getRotationSystem() = RotationSystemStandard()
  override fun getMinoColoring() = MinoColoringStandard()
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemStandard(width, viewHeight)
  override val moveAfterRotation = false
  override val upKeyNoLock = false
}

class RuleVariant: Rule {
  override fun getMinoRandomizer() = MinoRandomizerBag()
  override fun getRotationSystem() = RotationSystemVariant(false)
  override fun getMinoColoring() = MinoColoringVariant()
  override fun getSpawnSystem(width: Int, viewHeight: Int) = SpawnSystemVariant(width, viewHeight)
  override val moveAfterRotation = true
  override val upKeyNoLock = true
}
