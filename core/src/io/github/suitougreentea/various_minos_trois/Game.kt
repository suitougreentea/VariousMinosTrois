package io.github.suitougreentea.various_minos_trois

import java.util.*


class Game(val input: Input, width: Int, height: Int) {
    val bombSize = arrayOf (Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3), Pair(4, 4), Pair(4, 4), Pair(5, 5), Pair(5, 5), Pair(6, 6), Pair(6, 6), Pair(7, 7), Pair(7, 7), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8))

    val field = Field(width, height)

    val minoRandomizer: MinoRandomizer = MinoRandomizerBag(setOf(4, 5, 6, 7, 8, 9, 10))
    val minoColoring: MinoColoring = MinoColoringStandard()
    val minoGenerator: MinoGenerator = MinoGeneratorInfinite(6, minoRandomizer, minoColoring)
    //val minoGenerator: MinoGenerator = MinoGeneratorFinite()
    var currentMino = Mino(0, listOf(Pair(Pos(0, 0), BlockNormal(0))))
    var minoX = 0
    var minoY = 20
    var minoR = 0
    var ghostY = 0

    var holdMino: Mino? = null
    var alreadyHolded = false

    var speed = SpeedData(
            beforeMoving = 5,
            moveStart = 10,
            moveSpeed = 1f,
            drop = 22f,
            softDrop = 1f,
            lock = 10000000,
            forceLock = 10000000,
            afterMoving = 5,
            count = 0,
            beforeExplosion = 5,
            explosion = 20,
            afterExplosion = 5,
            cascade = 22f,
            afterCascade = 10,
            bigBomb = 10
    )

    var rotationSystem: RotationSystem = RotationSystemStandard()
    var spawnSystem: SpawnSystem = SpawnSystemStandard(width, 22)

    var moveDirection = 0
    var moveTimer = 0
    var moveStack = 0f
    var dropStack = 0f
    var softDropStack = 0f
    var lockTimer = 0
    var forceLockTimer = 0
    var countTimer = 0
    var explosionTimer = 0
    var cascadeStack = 0f
    var bigBombTimer = 0

    var explosionList: MutableList<ExplosionData> = ArrayList()
    var cascadeList: MutableList<CascadeData> = ArrayList()
    var bigBombList: MutableList<Pos> = ArrayList()

    val stateManager = StateManager(StateReady(30))

    var countLines: MutableList<Int> = ArrayList()
    var lastLines = 0
    var chain = 0
    var currentExplosionSize = 0

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

    fun attemptToMoveMino(dx: Int): Boolean {
        if(GameUtil.hitTestMino(field, currentMino, minoX + dx, minoY, minoR)) {
            return false
        } else {
            minoX += dx
            lockTimer = 0
            seQueue.add("landing")
            return true
        }
    }

    fun attemptToDropMino(): Boolean {
        if(GameUtil.hitTestMino(field, currentMino, minoX, minoY - 1, minoR)) {
            return false
        } else {
            minoY -= 1
            if(GameUtil.hitTestMino(field, currentMino, minoX, minoY - 1, minoR)) seQueue.add("landing")
            return true
        }
    }

    fun attemptToRotateMino(dr: Int): Boolean {
        val result = rotationSystem.attempt(field, currentMino, minoX, minoY, minoR, dr)
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
        currentMino.getRotatedBlocks(minoR).forEach { field[Pos(minoX + it.first.x, minoY + it.first.y)] = it.second }
        seQueue.add("lock")
    }

    fun update() {
        stateManager.update()
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

    enum class LineState {
        FILLED_WITHOUT_BOMB,
        FILLED_WITH_BOMB,
        NOT_FILLED
    }

    fun getLineState() = (0..field.height-1).map { iy ->
        val line = field.map.filter { it.key.y == iy }
        if(line.size == field.width) {
            if(line.any { it.value is BlockBomb || it.value is BlockBigBomb }) LineState.FILLED_WITH_BOMB else LineState.FILLED_WITHOUT_BOMB
        } else LineState.NOT_FILLED
    }

    fun isAnyLineFilled() = getLineState().any { it == LineState.FILLED_WITHOUT_BOMB || it == LineState.FILLED_WITH_BOMB }
    fun isExplodable() = getLineState().any { it == LineState.FILLED_WITH_BOMB }

    fun isBigBombNeeded() = field.map.filterValues { it is BlockBomb }.keys.any {
        arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1)).all {
            field.contains(it) && field.get(it) is BlockBomb
        }
    }

    fun getCurrentCascadeList(): MutableList<CascadeData> {
        val clonedFieldMap = HashMap(field.map)
        val list: MutableList<CascadeData> = ArrayList()

        fun getConnectedBlockSet(pos: Pos): List<Pair<Pos, Block>> {
            if(clonedFieldMap.contains(pos)) {
                var blockList: List<Pair<Pos, Block>> = arrayListOf(Pair(pos, clonedFieldMap.get(pos)!!))
                clonedFieldMap.remove(pos)
                if (clonedFieldMap.contains(Pos(pos.x + 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x + 1, pos.y))
                if (clonedFieldMap.contains(Pos(pos.x - 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x - 1, pos.y))
                if (clonedFieldMap.contains(Pos(pos.x, pos.y + 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y + 1))
                if (clonedFieldMap.contains(Pos(pos.x, pos.y - 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y - 1))
                return blockList
            } else throw IllegalStateException()
        }

        while(clonedFieldMap.size != 0) {
            val (position, block) = clonedFieldMap.entries.first()
            list.add(CascadeData(getConnectedBlockSet(position)))
        }

        return list
    }

    fun isCascadeNeeded() = getCurrentCascadeList().filter { it.blocks.all { it.first.y > 0 } }.size != 0

    inner abstract class StateWithTimer(val frames: Int): State {
        var timer = 0

        abstract fun nextState(): State

        override fun update() {
            if(timer == frames) stateManager.changeState(nextState())
            else timer ++
        }
    }

    inner class StateReady(frames: Int): StateWithTimer(frames) {
        override fun nextState() = StateBeforeMoving()
    }

    inner class StateBeforeMoving(): StateWithTimer(speed.beforeMoving) {
        override fun nextState() = StateMoving()
        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateMoving: State {
        override fun enter() {
            super.enter()
            val newMino = minoGenerator.poll()
            if(newMino == null) {
                throw UnsupportedOperationException("GameOver")
            }
            spawnNewMino(newMino)
            alreadyHolded = false
            if(input.c.isDown && !input.c.isPressed) {
                if(attemptToHoldMino()) seQueue.add("hold")
            }
            if(input.a.isDown && !input.a.isPressed) {
                if(attemptToRotateMino(-1)) seQueue.add("init_rotation")
            }
            if(input.b.isDown && !input.b.isPressed) {
                if(attemptToRotateMino(+1)) seQueue.add("init_rotation")
            }
            chain = 0
        }
        override fun update() {
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

                    kotlin.repeat(moveStack.toInt(), {
                        attemptToMoveMino(-1)
                    })
                    moveStack = moveStack % 1
                } else moveTimer ++
            }

            if(input.right.isDown && moveDirection == 1) {
                if(moveTimer == speed.moveStart) {
                    moveStack += speed.moveSpeed

                    kotlin.repeat(moveStack.toInt(), {
                        attemptToMoveMino(+1)
                    })
                    moveStack = moveStack % 1
                } else moveTimer ++
            }

            if(input.down.isDown) {
                softDropStack += speed.softDrop
                kotlin.repeat(softDropStack.toInt(), {
                    attemptToDropMino()
                })
                softDropStack = softDropStack % 1
            } else softDropStack = 0f

            dropStack += speed.drop
            kotlin.repeat(dropStack.toInt(), {
                attemptToDropMino()
            })
            dropStack = dropStack % 1

            if(input.up.isPressed) {
                while(!GameUtil.hitTestMino(field, currentMino, minoX, minoY - 1, minoR)) minoY --
                lockMino()
                stateManager.changeState(StateAfterMoving(speed.afterMoving))
            }

            if(GameUtil.hitTestMino(field, currentMino, minoX, minoY - 1, minoR)) {
                dropStack = 0f
                if(lockTimer == speed.lock || forceLockTimer == speed.forceLock) {
                    lockMino()
                    stateManager.changeState(StateAfterMoving(speed.afterMoving))
                } else {
                    lockTimer ++
                    forceLockTimer ++
                }
            } else lockTimer = 0

            ghostY = minoY
            while(!GameUtil.hitTestMino(field, currentMino, minoX, ghostY - 1, minoR)) ghostY--
        }
    }

    inner class StateAfterMoving(frames: Int): StateWithTimer(frames) {
        override fun nextState() = if(isAnyLineFilled() /* TODO: No, when line number changes */) StateCounting()
            else if(isCascadeNeeded()) StateCascade()
            else if(isBigBombNeeded()) StateMakingBigBomb()
            else StateBeforeMoving()

        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateCounting(): State {
        override fun update() {
            if(countTimer == speed.count) {
                countLines.clear()
                stateManager.changeState(
                        if(isExplodable()) StateBeforeExplosion()
                        else if(isCascadeNeeded()) StateCascade()
                        else if(isBigBombNeeded()) StateMakingBigBomb()
                        else StateBeforeMoving()
                )
            } else countTimer ++
            acceptMoveInput()
        }

        override fun leave() {
            super.leave()
            countTimer = 0
        }
    }

    inner class StateBeforeExplosion(): StateWithTimer(speed.beforeExplosion) {
        override fun nextState() = StateExplosion()
        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateExplosion(): State {
        override fun enter() {
            super.enter()
            getLineState().forEachIndexed { i, e -> if(e == LineState.FILLED_WITH_BOMB) {
                field.map.filter { it.key.y == i }.forEach {
                    val (pos, block) = it
                    if(block is BlockBomb) block.ignited = true
                    if(block is BlockBigBomb) {
                        block.ignited = true
                        when (block.position){
                            BlockBigBomb.Position.BOTTOM_LEFT, BlockBigBomb.Position.BOTTOM_RIGHT -> (field.get(Pos(pos.x, pos.y + 1)) as BlockBigBomb).ignited = true
                            BlockBigBomb.Position.TOP_LEFT, BlockBigBomb.Position.TOP_RIGHT -> (field.get(Pos(pos.x, pos.y - 1)) as BlockBigBomb).ignited = true
                        }
                    }
                }
            } }
            chain ++
            currentExplosionSize = chain + getLineState().filter { it == LineState.FILLED_WITHOUT_BOMB || it == LineState.FILLED_WITH_BOMB }.size - 2
        }
        override fun update() {
            if(explosionTimer == 0) {
                if(field.map.values.any { it is BlockBomb && it.ignited }) seQueue.add("explosion_small")
                if(field.map.values.any { it is BlockBigBomb && it.ignited }) seQueue.add("explosion_big")
                field.map.filterValues { it is BlockBomb && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, currentExplosionSize)) }
                field.map.filterValues { it is BlockBigBomb && it.position == BlockBigBomb.Position.BOTTOM_LEFT && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, -1)) }
            }
            if(explosionTimer == speed.explosion * 1/3) {
                explosionList.forEach {
                    val (cx, cy) = it.position
                    val (rx, ry) = if(it.size == -1) {
                        Pair((cx - 4 .. cx + 5), (cy - 4 .. cy + 5))
                    } else {
                        val (w, h) = bombSize.get(it.size)
                        Pair((cx - w .. cx + w), (cy - h .. cy + h))
                    }
                    field.remove(Pos(cx, cy))
                    if(it.size == -1) {
                        field.remove(Pos(cx + 1, cy))
                        field.remove(Pos(cx, cy + 1))
                        field.remove(Pos(cx + 1, cy + 1))
                    }
                    field.map.keys.filter { rx.contains(it.x) && ry.contains(it.y)}
                    .forEach {
                        val block = field.get(it)
                        if(block is BlockNormal) field.remove(it)
                        else if(block is BlockBomb) block.ignited = true
                        else if(block is BlockBigBomb) {
                            when(block.position) {
                                BlockBigBomb.Position.BOTTOM_LEFT -> arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y + 1), Pos(it.x + 1, it.y + 1))
                                BlockBigBomb.Position.BOTTOM_RIGHT -> arrayOf(Pos(it.x - 1, it.y), Pos(it.x, it.y), Pos(it.x - 1, it.y + 1), Pos(it.x, it.y + 1))
                                BlockBigBomb.Position.TOP_LEFT -> arrayOf(Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1), Pos(it.x, it.y), Pos(it.x + 1, it.y))
                                BlockBigBomb.Position.TOP_RIGHT -> arrayOf(Pos(it.x - 1, it.y - 1), Pos(it.x, it.y - 1), Pos(it.x - 1, it.y), Pos(it.x, it.y))
                            }.forEach { (field[it] as BlockBigBomb).ignited = true }
                        }
                    }
                }
            }
            if(explosionTimer == speed.explosion) {
                explosionTimer = 0
                explosionList = ArrayList()
                if(!field.map.any { val block = it.value; (block is BlockBomb && block.ignited) || (block is BlockBigBomb && block.ignited) }) stateManager.changeState(StateAfterExplosion())
            } else explosionTimer ++
            acceptMoveInput()
        }
    }

    inner class StateAfterExplosion(): StateWithTimer(speed.afterExplosion) {
        override fun nextState() = if(isCascadeNeeded()) StateCascade()
            else if(isBigBombNeeded()) StateMakingBigBomb()
            else StateBeforeMoving()

        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateCascade(): State {
        override fun enter() {
            super.enter()

            cascadeList = getCurrentCascadeList()
            field.clear()

            cascadeList.filter { it.blocks.any { it.first.y == 0 } }.forEach { it.blocks.forEach { field[it.first] = it.second } }
            cascadeList.removeAll { it.blocks.any { it.first.y == 0 } }
        }
        override fun update() {
            if(cascadeList.size == 0) {
                // Linecount may be needed
                stateManager.changeState(StateAfterCascade())
                return
            }
            cascadeStack += speed.cascade
            kotlin.repeat(cascadeStack.toInt(), {
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

    inner class StateAfterCascade(): StateWithTimer(speed.afterCascade) {
        override fun nextState() = if(isExplodable()) StateCounting()
            else if(isBigBombNeeded()) StateMakingBigBomb()
            else StateBeforeMoving()

        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateMakingBigBomb: State {
        override fun enter() {
            super.enter()
            val bigBombReservedList: MutableList<Pos> = ArrayList()
            // Sort: (0, 10), (1, 10), (0, 9), (1, 9) ...
            field.map.filterValues { it is BlockBomb }.keys.sortedWith(Comparator { a, b -> if(a.y == b.y) a.x - b.x else b.y - a.y }).forEach {
                val blockPositions = arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1))
                if(blockPositions.all { field.contains(it) && field.get(it) is BlockBomb && !bigBombReservedList.contains(it) }) {
                    bigBombList.add(Pos(it.x, it.y - 1))
                    blockPositions.forEach { bigBombReservedList.add(it) }
                }
            }
            bigBombReservedList.forEach { field.remove(it) }
            bigBombTimer = 0
            seQueue.add("big_bomb")
        }
        override fun update() {
            if(bigBombTimer == speed.bigBomb) {
                bigBombList.forEach {
                    field[Pos(it.x, it.y)] = BlockBigBomb(BlockBigBomb.Position.BOTTOM_LEFT)
                    field[Pos(it.x + 1, it.y)] = BlockBigBomb(BlockBigBomb.Position.BOTTOM_RIGHT)
                    field[Pos(it.x, it.y + 1)] = BlockBigBomb(BlockBigBomb.Position.TOP_LEFT)
                    field[Pos(it.x + 1, it.y + 1)] = BlockBigBomb(BlockBigBomb.Position.TOP_RIGHT)
                    stateManager.changeState(StateBeforeMoving())
                }
                bigBombList = ArrayList()
            } else bigBombTimer ++
            acceptMoveInput()
        }
    }

    // BigBomb is size=-1
    class ExplosionData(val position: Pos, val size: Int) {}
    class CascadeData(val blocks: List<Pair<Pos, Block>>) {}
    data class SpeedData(
            val beforeMoving: Int,
            val moveStart: Int,
            val moveSpeed: Float,
            val drop: Float,
            val softDrop: Float,
            val lock: Int,
            val forceLock: Int,
            val afterMoving: Int,
            val count: Int,
            val beforeExplosion: Int,
            val explosion: Int,
            val afterExplosion: Int,
            val cascade: Float,
            val afterCascade: Int,
            val bigBomb: Int
    )
}