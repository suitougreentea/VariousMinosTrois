package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.rule.*
import java.util.*

abstract class BasicMinoGame(val input: Input, val width: Int, val height: Int): Game {
  val field = Field(width, height)

  val minoRandomizer: MinoRandomizer = MinoRandomizerBag(setOf(0, 4, 5, 6, 7, 8, 9, 10))
  val minoColoring: MinoColoring = MinoColoringStandard()
  abstract val minoGenerator: MinoGenerator

  var rotationSystem: RotationSystem = RotationSystemStandard()
  var spawnSystem: SpawnSystem = SpawnSystemStandard(width, 22)

  //val minoGenerator: MinoGenerator = MinoGeneratorFinite()
  var currentMino: Mino? = null
  var minoX = 0
  var minoY = 20
  var minoR = 0
  var ghostY = 0

  var holdMino: Mino? = null
  var alreadyHolded = false

  var speed = SpeedDataBasicMino(
          beforeMoving = 5,
          moveStart = 10,
          moveSpeed = 1f,
          drop = 1 / 60f,
          softDrop = 1f,
          lock = 60,
          forceLock = 180,
          afterMoving = 5
  )

  var moveDirection = 0
  var moveTimer = 0
  var moveStack = 0f
  var dropStack = 0f
  var softDropStack = 0f
  var lockTimer = 0
  var forceLockTimer = 0

  val stateManager = StateManager()

  val seQueue: MutableSet<String> = HashSet()

  fun spawnNewMino(mino: Mino) {
    currentMino = mino
    val spawnData = spawnSystem.get(mino.minoId)
    minoX = spawnData.position.x
    minoY = spawnData.position.y
    minoR = spawnData.rotation
    dropStack = 0f
    softDropStack = 0f
    lockTimer = 0
    forceLockTimer = 0
  }

  open fun newCycle() { }

  fun attemptToMoveMino(dx: Int): Boolean {
    val mino = currentMino ?: return false
    if(GameUtil.hitTestMino(field, mino, minoX + dx, minoY, minoR)) {
      return false
    } else {
      minoX += dx
      lockTimer = 0
      if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) seQueue.add("landing")
      return true
    }
  }

  fun attemptToDropMino(): Boolean {
    val mino = currentMino ?: return false
    if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
      return false
    } else {
      minoY -= 1
      if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) seQueue.add("landing")
      return true
    }
  }

  fun attemptToRotateMino(dr: Int): Boolean {
    val mino = currentMino ?: return false
    val result = rotationSystem.attempt(field, mino, minoX, minoY, minoR, dr)
    if(result.success) {
      minoX += result.offset.x
      minoY += result.offset.y
      minoR = (minoR + 4 + dr) % 4
      lockTimer = 0
      return true
    } else {
      return false
    }
  }

  fun attemptToHoldMino(): Boolean {
    if(alreadyHolded) {
      return false
    } else {
      val currentHoldMino = holdMino
      if(currentHoldMino == null) {
        holdMino = currentMino
        currentMino = minoGenerator.poll()!!
      } else {
        holdMino = currentMino
        spawnNewMino(currentHoldMino)
      }
      alreadyHolded = true
      return true
    }
  }

  fun lockMino() {
    val mino = currentMino ?: return
    mino.getRotatedBlocks(minoR).forEach { field[Pos(minoX + it.first.x, minoY + it.first.y)] = it.second }
    seQueue.add("lock")
  }

  fun acceptMoveInput() {
    if(input.left.isPressed) {
      moveDirection = -1
      moveTimer = 0
      moveStack = 0f
    }
    if(input.right.isPressed) {
      moveDirection = 1
      moveTimer = 0
      moveStack = 0f
    }

    if(input.left.isDown && moveDirection == -1) {
      if(moveTimer < speed.moveStart) moveTimer ++
    }

    if(input.right.isDown && moveDirection == 1) {
      if(moveTimer < speed.moveStart) moveTimer ++
    }
  }

  open fun newStateReady() = StateReady()
  open fun newStateBeforeMoving() = StateBeforeMoving()
  open fun newStateMoving() = StateMoving()
  open fun newStateAfterMoving() = StateAfterMoving()

  open inner class StateReady: StateWithTimer() {
    override val frames = 30
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateBeforeMoving()
  }

  open inner class StateBeforeMoving: StateWithTimer() {
    override val frames = speed.beforeMoving
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateMoving()
    override fun update() {
      super.update()
      acceptMoveInput()
    }
  }
  open inner class StateMoving: State {
    override fun enter() {
      super.enter()
      val newMino: Mino?
      alreadyHolded = false
      if(input.c.isDown && !input.c.isPressed) {
        val currentHoldMino = holdMino
        if(currentHoldMino == null) {
          holdMino = minoGenerator.poll()
          newMino = minoGenerator.poll()
        } else {
          holdMino = minoGenerator.poll()
          newMino = currentHoldMino
        }
        seQueue.add("hold")
      } else {
        newMino = minoGenerator.poll()
      }

      if(newMino == null) {
        throw UnsupportedOperationException("GameOver")
      }
      spawnNewMino(newMino)
      newCycle()

      if(input.a.isDown && !input.a.isPressed) {
        if(attemptToRotateMino(-1)) seQueue.add("init_rotation")
      }
      if(input.b.isDown && !input.b.isPressed) {
        if(attemptToRotateMino(+1)) seQueue.add("init_rotation")
      }
    }
    override fun update() {
      val mino = currentMino ?: return

      if(input.a.isPressed) {
        if(attemptToRotateMino(-1)) seQueue.add("rotation") else seQueue.add("rotation_fail")
      }
      if(input.b.isPressed) {
        if(attemptToRotateMino(+1)) seQueue.add("rotation") else seQueue.add("rotation_fail")
      }
      if(input.c.isPressed) {
        if(attemptToHoldMino()) seQueue.add("hold")
      }

      if(input.left.isPressed) {
        moveDirection = -1
        moveTimer = 0
        moveStack = 0f
        attemptToMoveMino(-1)
      }
      if(input.right.isPressed) {
        moveDirection = 1
        moveTimer = 0
        moveStack = 0f
        attemptToMoveMino(+1)
      }

      if(input.left.isDown && moveDirection == -1) {
        if(moveTimer == speed.moveStart) {
          moveStack += speed.moveSpeed

          repeat(moveStack.toInt(), {
            attemptToMoveMino(-1)
          })
          moveStack = moveStack % 1
        } else moveTimer ++
      }

      if(input.right.isDown && moveDirection == 1) {
        if(moveTimer == speed.moveStart) {
          moveStack += speed.moveSpeed

          repeat(moveStack.toInt(), {
            attemptToMoveMino(+1)
          })
          moveStack = moveStack % 1
        } else moveTimer ++
      }

      if(input.down.isDown) {
        softDropStack += speed.softDrop
        repeat(softDropStack.toInt(), {
          attemptToDropMino()
        })
        softDropStack = softDropStack % 1
      } else softDropStack = 0f

      dropStack += speed.drop
      repeat(dropStack.toInt(), {
        attemptToDropMino()
      })
      dropStack = dropStack % 1

      if(input.up.isPressed) {
        while(!GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) minoY --
        lockMino()
        stateManager.changeState(newStateAfterMoving())
      }

      if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
        dropStack = 0f
        if(lockTimer == speed.lock || forceLockTimer == speed.forceLock) {
          lockMino()
          stateManager.changeState(newStateAfterMoving())
        } else {
          lockTimer ++
          forceLockTimer ++
        }
      } else lockTimer = 0

      ghostY = minoY
      while(!GameUtil.hitTestMino(field, mino, minoX, ghostY - 1, minoR)) ghostY--
    }
  }

  open inner class StateAfterMoving: StateWithTimer() {
    override val frames = speed.afterMoving
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState(): State = newStateBeforeMoving()

    override fun update() {
      super.update()
      acceptMoveInput()
    }
  }

  override fun init() {
    stateManager.changeState(newStateReady())
  }

  override fun update() {
    stateManager.update()
  }

  data class SpeedDataBasicMino(
          val beforeMoving: Int,
          val moveStart: Int,
          val moveSpeed: Float,
          val drop: Float,
          val softDrop: Float,
          val lock: Int,
          val forceLock: Int,
          val afterMoving: Int
  )
}
