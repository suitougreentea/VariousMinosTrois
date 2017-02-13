package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.rule.*
import java.util.*

abstract class BasicMinoGame(val input: Input, val width: Int, val height: Int): Game {
  val field = Field(width, height)

  abstract val minoBuffer: MinoBuffer
  //val minoBuffer: MinoBuffer = MinoBufferFinite()

  var rotationSystem: RotationSystem = RotationSystemStandard()
  var spawnSystem: SpawnSystem = SpawnSystemStandard(width, 22)

  var currentMino: Mino? = null
  var minoX = 0
  var minoY = 20
  var minoR = 0
  var ghostY = 0

  var holdMino: Mino? = null
  var alreadyHolded = false

  var background = 0

  open var speed = SpeedDataBasicMino(
          beforeMoving = 5,
          moveStart = 10,
          moveSpeed = 1f,
          drop = 1 / 60f,
          softDrop = 1f,
          lock = 60,
          forceLock = 180,
          afterMoving = 5,
          cascade = 22f,
          afterCascade = 10
  )

  var moveDirection = 0
  var moveTimer = 0
  var moveStack = 0f
  var dropStack = 0f
  var softDropStack = 0f
  var lockTimer = 0
  var forceLockTimer = 0
  var cascadeStack = 0f
  var lockRenderTimer = -1

  var drop = 0

  var lockRenderList: List<Pos> = ArrayList()

  val stateManager = StateManager()

  var cascadeList: MutableList<CascadeData> = ArrayList()

  val seQueue: MutableSet<String> = HashSet()

  var enableTimer = false
  var gameTimer = 0

  open fun spawnNewMino(mino: Mino) {
    currentMino = mino
    val spawnData = spawnSystem.get(mino.minoId)
    minoX = spawnData.position.x
    minoY = spawnData.position.y
    minoR = spawnData.rotation
    dropStack = 0f
    softDropStack = 0f
    lockTimer = 0
    forceLockTimer = 0
    lockRenderTimer = -1
    drop = 0
  }

  open fun newCycle() { }

  open fun attemptToMoveMino(dx: Int): Boolean {
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

  open fun attemptToDropMino(): Boolean {
    val mino = currentMino ?: return false
    if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
      return false
    } else {
      minoY -= 1
      if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) seQueue.add("landing")
      return true
    }
  }

  open fun attemptToRotateMino(dr: Int): Boolean {
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

  open fun attemptToHoldMino(): Boolean {
    if(alreadyHolded) {
      return false
    } else {
      val currentHoldMino = holdMino
      if(currentHoldMino == null) {
        holdMino = currentMino
        val newMino = minoBuffer.poll()!!
        currentMino = newMino
        spawnNewMino(newMino)
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
    lockRenderTimer = 0
    lockRenderList = mino.getRotatedBlocks(minoR).map { Pos(minoX + it.first.x, minoY + it.first.y) }
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
  open fun newStateCascade() = StateCascade()
  open fun newStateAfterCascade() = StateAfterCascade()

  open inner class StateReady: StateWithTimer() {
    override val frames = 30
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateBeforeMoving()
    override fun leave() {
      super.leave()
      enableTimer = true
    }
  }

  open inner class StateBeforeMoving: StateWithTimer() {
    override val frames = speed.beforeMoving
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateMoving()
    override fun update() {
      super.update()
      if(lockRenderTimer >= 0) lockRenderTimer ++
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
          holdMino = minoBuffer.poll()
          newMino = minoBuffer.poll()
        } else {
          holdMino = minoBuffer.poll()
          newMino = currentHoldMino
        }
        alreadyHolded = true
        seQueue.add("hold")
      } else {
        newMino = minoBuffer.poll()
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

      dropStack += speed.drop
      repeat(dropStack.toInt(), {
        attemptToDropMino()
      })
      dropStack = dropStack % 1
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
          if(attemptToDropMino()) drop ++
        })
        softDropStack = softDropStack % 1
      } else softDropStack = 0f

      dropStack += speed.drop
      repeat(dropStack.toInt(), {
        attemptToDropMino()
      })
      dropStack = dropStack % 1

      if(input.up.isPressed) {
        while(!GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
          minoY --
          drop ++
        }
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
      if(lockRenderTimer >= 0) lockRenderTimer ++
      acceptMoveInput()
    }
  }

  open inner class StateCascade(): State {
    override fun enter() {
      super.enter()

      cascadeList = getCurrentCascadeList()
      field.clear()

      cascadeList.filter { it.blocks.any { it.first.y == 0 } }.forEach { it.blocks.forEach { field[it.first] = it.second } }
      cascadeList.removeAll { it.blocks.any { it.first.y == 0 } }
      cascadeList.removeAll { it.blocks.any { it.second.fixed } }
    }
    override fun update() {
      if(cascadeList.size == 0) {
        // Linecount may be needed
        stateManager.changeState(newStateAfterCascade())
        return
      }
      cascadeStack += speed.cascade
      repeat(cascadeStack.toInt(), {
        val currentCascadeList = ArrayList(cascadeList)
        cascadeList.removeAll {true}
        currentCascadeList.forEach {
          if(it.blocks.any { it.first.y == 0 || field.contains(Pos(it.first.x, it.first.y - 1)) }) {
            it.blocks.forEach {
              field[it.first] = it.second
            }
          } else {
            cascadeList.add(CascadeData(it.blocks.map { Pair(Pos(it.first.x, it.first.y - 1), it.second) }))
          }
        }
      })
      cascadeStack %= 1
      acceptMoveInput()
    }
    override fun leave() {
      super.leave()
      cascadeStack = 0f
    }
  }

  open inner class StateAfterCascade: StateWithTimer() {
    override val frames = speed.afterCascade
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState(): State = newStateBeforeMoving()

    override fun update() {
      super.update()
      acceptMoveInput()
    }
  }

  open fun getCurrentCascadeList(): MutableList<CascadeData> {
    val clonedFieldMap = HashMap(field.map)
    val list: MutableList<CascadeData> = ArrayList()

    fun getConnectedBlockSet(pos: Pos): List<Pair<Pos, Block>> {
      val block = clonedFieldMap.get(pos)
      if(block != null && block is Block) {
        var blockList: List<Pair<Pos, Block>> = arrayListOf(Pair(pos, block))
        clonedFieldMap.remove(pos)
        if (clonedFieldMap.contains(Pos(pos.x + 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x + 1, pos.y))
        if (clonedFieldMap.contains(Pos(pos.x - 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x - 1, pos.y))
        if (clonedFieldMap.contains(Pos(pos.x, pos.y + 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y + 1))
        if (clonedFieldMap.contains(Pos(pos.x, pos.y - 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y - 1))
        return blockList
      } else throw IllegalStateException()
    }

    while(clonedFieldMap.size != 0) {
      val (position, _) = clonedFieldMap.entries.first()
      list.add(CascadeData(getConnectedBlockSet(position)))
    }

    list.sortBy { it.blocks.minBy { it.first.y }?.first?.y ?: 0 }

    return list
  }

  override fun init() {
    stateManager.changeState(newStateReady())
  }

  override fun update() {
    stateManager.update()
    if(enableTimer) gameTimer++
  }

  data class SpeedDataBasicMino(
          var beforeMoving: Int,
          var moveStart: Int,
          var moveSpeed: Float,
          var drop: Float,
          var softDrop: Float,
          var lock: Int,
          var forceLock: Int,
          var afterMoving: Int,
          var cascade: Float,
          var afterCascade: Int
  )

  class CascadeData(val blocks: List<Pair<Pos, Block>>)
}
