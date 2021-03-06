package io.github.suitougreentea.various_minos_trois.game

import io.github.suitougreentea.various_minos_trois.*
import io.github.suitougreentea.various_minos_trois.rule.*
import io.github.suitougreentea.various_minos_trois.rule.Rule
import java.util.*

abstract class BasicMinoGame(val player: Player, val width: Int, val height: Int, val rule: Rule): Game {
  override fun getRequiredRenderer(app: VariousMinosTrois) = BasicMinoRenderer(app, player.playerNumber)
  val minoRandomizer = rule.getMinoRandomizer()
  val minoColoring = rule.getMinoColoring()
  var rotationSystem: RotationSystem = rule.getRotationSystem()
  var spawnSystem: SpawnSystem = rule.getSpawnSystem(width, 22)

  init {
    minoRandomizer.newMinoSet(setOf(4, 5, 6, 7, 8, 9, 10))
  }

  val bufferNum = if(rule.multiNextAndHold) 6 else 1
  val enableHold = rule.multiNextAndHold

  val input = player.input

  val field = Field(width, height)

  abstract val minoBuffer: MinoBuffer
  //val minoBuffer: MinoBuffer = MinoBufferFinite()

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
  var moveTimer = -1
  var moveStack = 0f
  var dropStack = 0f
  var softDropStack = 0f
  var lockTimer = -1
  var forceLockTimer = -1
  var cascadeStack = 0f
  var lockRenderTimer = -1

  var dropLockUsed = false

  var droppedBlocks = 0

  var lockRenderList: List<Pos> = ArrayList()

  val stateManager = StateManager()

  var cascadeList: MutableList<CascadeData> = ArrayList()

  val seQueue: MutableSet<String> = HashSet()

  var enableTimer = false
  var gameTimer = 0
  var gameOver = false

  val nextFrameTaskList: MutableList<NextFrameTask> = ArrayList()

  val log: MutableList<String> = LinkedList()

  open fun spawnNewMino(mino: Mino) {
    currentMino = mino
    val spawnData = spawnSystem.get(mino.minoId)
    minoX = spawnData.position.x
    minoY = spawnData.position.y
    minoR = spawnData.rotation
    rotationSystem.reset()
    dropStack = 0f
    softDropStack = 0f
    lockTimer = 0
    forceLockTimer = 0
    lockRenderTimer = -1
    droppedBlocks = 0
    if(GameUtil.hitTestMino(field, mino, minoX, minoY, minoR)) gameOver = true
  }

  open fun onNewCycle() { }

  fun handleMove() {
    if(input.left.isPressed) {
      moveDirection = -1
      moveTimer = 0
      moveStack = 0f
      if(speed.moveStart > 1) attemptToMoveMino(-1)
    }
    if(input.right.isPressed) {
      moveDirection = 1
      moveTimer = 0
      moveStack = 0f
      if(speed.moveStart > 1) attemptToMoveMino(+1)
    }

    if(input.left.isDown && moveDirection == -1) {
      moveTimer ++
      if(moveTimer >= speed.moveStart) {
        moveStack += speed.moveSpeed

        repeat(moveStack.toInt(), {
          attemptToMoveMino(-1)
        })
        moveStack %= 1
      }
    }

    if(!input.left.isDown && !input.right.isDown) moveTimer = -1

    if(input.right.isDown && moveDirection == 1) {
      moveTimer ++
      if(moveTimer >= speed.moveStart) {
        moveStack += speed.moveSpeed

        repeat(moveStack.toInt(), {
          attemptToMoveMino(+1)
        })
        moveStack %= 1
      }
    }
  }

  fun handleRotate() {
    if(input.a.isPressed) {
      if(attemptToRotateMino(-1)) seQueue.add("rotation") else seQueue.add("rotation_fail")
    }
    if(input.b.isPressed) {
      if(attemptToRotateMino(+1)) seQueue.add("rotation") else seQueue.add("rotation_fail")
    }
  }

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
    if(!enableHold) return false
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

  fun acceptMoveAccumulation() {
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

    if(!input.left.isDown && !input.right.isDown) moveTimer = -1

    if(input.left.isDown && moveDirection == -1) {
      moveTimer ++
    }

    if(input.right.isDown && moveDirection == 1) {
      moveTimer ++
    }

    if(!input.down.isDown) dropLockUsed = false
  }

  open fun newStateReady() = StateReady()
  open fun newStateBeforeMoving() = StateBeforeMoving()
  open fun newStateMoving() = StateMoving()
  open fun newStateAfterMoving() = StateAfterMoving()
  open fun newStateCascade() = StateCascade()
  open fun newStateAfterCascade() = StateAfterCascade()

  open inner class StateReady: StateWithTimer() {
    override val frames = 120
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateMoving()
    override fun leave() {
      super.leave()
      enableTimer = true
    }
  }

  open inner class StateBeforeMoving: StateWithTimer() {
    override val frames = speed.beforeMoving
    override val stateManager = this@BasicMinoGame.stateManager

    override fun nextState() = newStateMoving()
  }
  open inner class StateMoving: State {
    override fun enter() {
      super.enter()
      val newMino: Mino?
      if(input.c.isDown && !input.c.isPressed && enableHold) {
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
        alreadyHolded = false
      }

      if(newMino == null) {
        throw UnsupportedOperationException("GameOver")
      }
      spawnNewMino(newMino)
      onNewCycle()

      if(input.a.isDown && !input.a.isPressed && rule.initialRotation) {
        if(attemptToRotateMino(-1)) seQueue.add("init_rotation")
      }
      if(input.b.isDown && !input.b.isPressed && rule.initialRotation) {
        if(attemptToRotateMino(+1)) seQueue.add("init_rotation")
      }

      dropStack += speed.drop
      repeat(dropStack.toInt(), {
        attemptToDropMino()
      })
      dropStack %= 1
    }
    override fun update() {
      val mino = currentMino ?: return

      if(rule.moveAfterRotation) {
        handleRotate()
        handleMove()
      } else {
        handleMove()
        handleRotate()
      }

      if(input.c.isPressed) {
        if(attemptToHoldMino()) seQueue.add("hold")
      }

      if(input.down.isDown) {
        if(!rule.upKeyLock && GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR) && !dropLockUsed) {
          if(rule.alternativeLock) {
            lockTimer += 2
          } else {
            dropLockUsed = true
            lockMino()
            stateManager.changeState(newStateAfterMoving())
          }
        } else {
          softDropStack += speed.softDrop
          repeat(softDropStack.toInt(), {
            if (attemptToDropMino()) droppedBlocks++
          })
          softDropStack %= 1
        }
      } else {
        softDropStack = 0f
        dropLockUsed = false
      }

      dropStack += speed.drop
      repeat(dropStack.toInt(), {
        attemptToDropMino()
      })
      dropStack %= 1

      if(input.up.isPressed) {
        while(!GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
          minoY --
          droppedBlocks ++
        }
        if(rule.upKeyLock) {
          lockMino()
          stateManager.changeState(newStateAfterMoving())
        }
      }

      if(GameUtil.hitTestMino(field, mino, minoX, minoY - 1, minoR)) {
        dropStack = 0f
        lockTimer ++
        forceLockTimer ++
        if(lockTimer >= speed.lock || forceLockTimer == speed.forceLock) {
          lockMino()
          stateManager.changeState(newStateAfterMoving())
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
    minoBuffer.init()
    stateManager.changeState(newStateReady())
  }

  override fun update() {
    nextFrameTaskList.filter { !it.notExecuteOnPaused }.forEach { it.function(); nextFrameTaskList.remove(it) }
    if(gameOver) return
    nextFrameTaskList.forEach { it.function(); nextFrameTaskList.remove(it) }
    stateManager.update()
    if(stateManager.currentState !is StateMoving) acceptMoveAccumulation()
    if(enableTimer) gameTimer++
    if(lockRenderTimer >= 0) lockRenderTimer ++
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
  class NextFrameTask(val function: () -> Unit, val notExecuteOnPaused: Boolean)
}
