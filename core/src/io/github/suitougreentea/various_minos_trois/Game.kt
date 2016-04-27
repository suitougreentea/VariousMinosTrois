package io.github.suitougreentea.various_minos_trois

import java.util.*


class Game(val input: Input, val width: Int, val height: Int) {
    val bombSize = arrayOf (Pair(3, 0), Pair(3, 1), Pair(3, 2), Pair(3, 3), Pair(4, 4), Pair(4, 4), Pair(5, 5), Pair(5, 5), Pair(6, 6), Pair(6, 6), Pair(7, 7), Pair(7, 7), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8), Pair(8, 8))

    /*val field: Array<Array<Block?>> = Array(height, { Array<Block?>(width, { null }) })
    fun emptifyField() {
        field.forEachIndexed { iy, row -> row.forEachIndexed { ix, block -> field[iy][ix] = null } }
    }*/
    val field: MutableMap<Pos, Block> = HashMap()

    val minoDecorator = MinoDecoratorDefault()
    var minoId = 0
    var currentMino = Mino(minoId, minoDecorator)
    var minoX = 0
    var minoY = 20
    var minoR = 0
    var ghostY = 0

    val minoRandomizer = MinoRandomizer()
    var nextMinos: MutableList<Mino> = arrayListOf()
    init {
        kotlin.repeat(6) { nextMinos.add(Mino(minoRandomizer.next(), minoDecorator)) }
    }
    var holdMino: Mino? = null
    var alreadyHolded = false

    var moveDirection = 0
    var moveTimer = 0
    val moveTimerMax = 10
    var moveStack = 0f
    val moveSpeed = 1f

    var dropStack = 0f
    val dropSpeed = 1/60f
    var softDropStack = 0f
    val softDropSpeed = 1f

    var lockTimer = 0
    val lockTimerMax = 60

    var forceLockTimer = 0
    val forceLockTimerMax = 180

    var explosionTimer = 0
    var explosionTimerMax = 30

    var cascadeStack = 0f
    var cascadeSpeed = 1f

    var bigBombTimer = 0
    var bigBombTimerMax = 60

    var explosionList: MutableList<ExplosionData> = ArrayList()
    var cascadeList: MutableList<CascadeData> = ArrayList()
    var bigBombList: MutableList<Pos> = ArrayList()

    var currentState: State = StateReady(10)

    var chain = 0
    var currentExplosionSize = 0

    init {
    }

    fun spawnNewMino(mino: Mino) {
        currentMino = mino
        minoX = 3
        minoY = 20
        minoR = 0
        dropStack = 0f
        softDropStack = 0f
        lockTimer = 0
        forceLockTimer = 0
    }

    fun attemptToMoveMino(dx: Int, dy: Int): Boolean {
        if(hitTestMino(currentMino, minoX + dx, minoY + dy, minoR)) {
            return false
        } else {
            minoX += dx
            minoY += dy
            return true
        }
    }

    fun attemptToRotateMino(dr: Int): Boolean {
        if(hitTestMino(currentMino, minoX, minoY, (minoR + 4 + dr) % 4)) {
            return false
        } else {
            minoR = (minoR + 4 + dr) % 4
            return true
        }
    }

    fun attemptToHoldMino(): Boolean {
        if(alreadyHolded) {
            return false
        } else {
            val currentHoldMino = holdMino
            if(currentHoldMino == null) {
                holdMino = currentMino
                currentMino = nextMinos.removeAt(0)
                nextMinos.add(Mino(minoRandomizer.next(), minoDecorator))
            } else {
                holdMino = currentMino
                spawnNewMino(currentHoldMino)
            }
            alreadyHolded = true
            return true
        }
    }

    fun hitTestMino(mino: Mino, x: Int, y: Int, r: Int): Boolean {
        val blocks = mino.getRotatedBlocks(r)
        return blocks.any { e ->
            val dx = x + e.first.x
            val dy = y + e.first.y
            (dx < 0 || width <= dx || dy < 0 || height <= dy || field.containsKey(Pos(dx, dy)))
        }
    }

    fun lockMino() {
        currentMino.getRotatedBlocks(minoR).forEach { field.set(Pos(minoX + it.first.x, minoY + it.first.y), it.second) }
    }

    fun update() {
        currentState.update()
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
            if(moveTimer < moveTimerMax) moveTimer ++
        }

        if(input.right.isDown && moveDirection == 1) {
            if(moveTimer < moveTimerMax) moveTimer ++
        }
    }

    enum class LineState {
        FILLED_WITHOUT_BOMB,
        FILLED_WITH_BOMB,
        NOT_FILLED
    }

    fun getLineState() = (0..height-1).map { iy ->
        val line = field.filter { it.key.y == iy }
        if(line.size == width) {
            if(line.any { it.value is BlockBomb || it.value is BlockBigBomb }) LineState.FILLED_WITH_BOMB else LineState.FILLED_WITHOUT_BOMB
        } else LineState.NOT_FILLED
    }

    fun isBigBombNeeded() = field.filterValues { it is BlockBomb }.keys.any {
        arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1)).all {
            field.containsKey(it) && field.get(it) is BlockBomb
        }
    }

    fun changeState(newState: State) {
        currentState.leave()
        currentState = newState
        currentState.enter()
        currentState.update()
    }

    interface State {
        fun enter() {}
        fun update()
        fun leave() {}
    }

    inner abstract class StateWithTimer(val frames: Int): State {
        var timer = 0

        abstract fun nextState(): State

        override fun update() {
            if(timer == frames) changeState(nextState())
            else timer ++
        }
    }

    inner class StateReady(frames: Int): StateWithTimer(frames) {
        override fun nextState() = StateMoving()
        override fun leave() {
        }
    }
    inner class StateMoving: State {
        override fun enter() {
            super.enter()
            nextMinos.add(Mino(minoRandomizer.next(), minoDecorator))
            spawnNewMino(nextMinos.removeAt(0))
            alreadyHolded = false
            if(input.c.isDown && !input.c.isPressed) attemptToHoldMino()
            if(input.a.isDown && !input.a.isPressed) {
                attemptToRotateMino(-1)
            }
            if(input.b.isDown && !input.b.isPressed) {
                attemptToRotateMino(+1)
            }
            chain = 0
        }
        override fun update() {
            if(input.left.isPressed) {
                moveDirection = -1
                moveTimer = 0
                moveStack = 0f
                attemptToMoveMino(-1, 0)
            }
            if(input.right.isPressed) {
                moveDirection = 1
                moveTimer = 0
                moveStack = 0f
                attemptToMoveMino(+1, 0)
            }

            if(input.left.isDown && moveDirection == -1) {
                if(moveTimer == moveTimerMax) {
                    moveStack += moveSpeed

                    kotlin.repeat(moveStack.toInt(), {
                        attemptToMoveMino(-1, 0)
                    })
                    moveStack = moveStack % 1
                } else moveTimer ++
            }

            if(input.right.isDown && moveDirection == 1) {
                if(moveTimer == moveTimerMax) {
                    moveStack += moveSpeed

                    kotlin.repeat(moveStack.toInt(), {
                        attemptToMoveMino(+1, 0)
                    })
                    moveStack = moveStack % 1
                } else moveTimer ++
            }

            if(input.down.isDown) {
                softDropStack += softDropSpeed
                kotlin.repeat(softDropStack.toInt(), {
                    attemptToMoveMino(0, -1)
                })
                softDropStack = softDropStack % 1
            } else softDropStack = 0f

            if(input.up.isPressed) {
                while(!hitTestMino(currentMino, minoX, minoY - 1, minoR)) minoY --
                lockMino()
                changeState(StateAfterMoving(10))
            }

            if(input.a.isPressed) attemptToRotateMino(-1)
            if(input.b.isPressed) attemptToRotateMino(+1)
            if(input.c.isPressed) attemptToHoldMino()

            dropStack += dropSpeed
            kotlin.repeat(dropStack.toInt(), {
                attemptToMoveMino(0, -1)
            })
            dropStack = dropStack % 1

            if(hitTestMino(currentMino, minoX, minoY - 1, minoR)) {
                dropStack = 0f
                if(lockTimer == lockTimerMax || forceLockTimer == forceLockTimerMax) {
                    lockMino()
                    changeState(StateAfterMoving(20))
                } else {
                    lockTimer ++
                    forceLockTimer ++
                }
            } else lockTimer = 0

            ghostY = minoY
            while(!hitTestMino(currentMino, minoX, ghostY - 1, minoR)) ghostY--
        }
    }
    inner class StateAfterMoving(frames: Int): StateWithTimer(frames) {
        override fun nextState(): State {
            if(getLineState().any { it == LineState.FILLED_WITH_BOMB }) return StateExplosion()
            else if(isBigBombNeeded()) return StateMakingBigBomb()
            else return StateMoving()
        }
        override fun update() {
            super.update()
            acceptMoveInput()
        }
    }

    inner class StateExplosion(): State {
        override fun enter() {
            super.enter()
            getLineState().forEachIndexed { i, e -> if(e == LineState.FILLED_WITH_BOMB) {
                field.filter { it.key.y == i }.forEach {
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
                field.filterValues { it is BlockBomb && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, currentExplosionSize)) }
                field.filterValues { it is BlockBigBomb && it.position == BlockBigBomb.Position.BOTTOM_LEFT && it.ignited }.forEach { explosionList.add(ExplosionData(it.key, -1)) }
            }
            if(explosionTimer == explosionTimerMax * 2/3) {
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
                    field.keys.filter { rx.contains(it.x) && ry.contains(it.y)}
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
            if(explosionTimer == explosionTimerMax) {
                explosionTimer = 0
                explosionList = ArrayList()
                if(!field.any { val block = it.value; (block is BlockBomb && block.ignited) || (block is BlockBigBomb && block.ignited) }) changeState(StateCascade())
            } else explosionTimer ++
        }
    }
    inner class StateCascade(): State {
        override fun enter() {
            super.enter()
            //val list: MutableMap<Pair<Int, Int>, Block> = HashMap(field)

            fun getConnectedBlockSet(pos: Pos): List<Pair<Pos, Block>> {
                if(field.contains(pos)) {
                    var blockList: List<Pair<Pos, Block>> = arrayListOf(Pair(pos, field.get(pos)!!))
                    field.remove(pos)
                    if (field.contains(Pos(pos.x + 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x + 1, pos.y))
                    if (field.contains(Pos(pos.x - 1, pos.y))) blockList += getConnectedBlockSet(Pos(pos.x - 1, pos.y))
                    if (field.contains(Pos(pos.x, pos.y + 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y + 1))
                    if (field.contains(Pos(pos.x, pos.y - 1))) blockList += getConnectedBlockSet(Pos(pos.x, pos.y - 1))
                    return blockList
                } else throw IllegalStateException()
            }

            while(field.size != 0) {
                val (position, block) = field.entries.first()
                cascadeList.add(CascadeData(getConnectedBlockSet(position)))
            }

            //emptifyField()

            cascadeList.filter { it.blocks.any { it.first.y == 0 } }.forEach { it.blocks.forEach { field.put(it.first, it.second) } }
            cascadeList.removeAll { it.blocks.any { it.first.y == 0 } }
        }
        override fun update() {
            if(cascadeList.size == 0) {
                changeState(
                        if(getLineState().any { it == LineState.FILLED_WITH_BOMB }) StateExplosion()
                        else StateMoving()
                )
                return
            }
            cascadeStack += cascadeSpeed
            kotlin.repeat(cascadeStack.toInt(), {
                val currentCascadeList = ArrayList(cascadeList)
                cascadeList.removeAll {true}
                currentCascadeList.forEach {
                    if(it.blocks.any { it.first.y == 0 || field.containsKey(Pos(it.first.x, it.first.y - 1)) }) {
                        it.blocks.forEach {
                            field.put(it.first, it.second)
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
    inner class StateMakingBigBomb: State {
        override fun enter() {
            super.enter()
            val bigBombReservedList: MutableList<Pos> = ArrayList()
            // Sort: (0, 10), (1, 10), (0, 9), (1, 9) ...
            field.filterValues { it is BlockBomb }.keys.sortedWith(Comparator { a, b -> if(a.y == b.y) a.x - b.x else b.y - a.y }).forEach {
                val blockPositions = arrayOf(Pos(it.x, it.y), Pos(it.x + 1, it.y), Pos(it.x, it.y - 1), Pos(it.x + 1, it.y - 1))
                if(blockPositions.all { field.containsKey(it) && field.get(it) is BlockBomb && !bigBombReservedList.contains(it) }) {
                    bigBombList.add(Pos(it.x, it.y - 1))
                    blockPositions.forEach { bigBombReservedList.add(it) }
                }
            }
            bigBombReservedList.forEach { field.remove(it) }
            bigBombTimer = 0
        }
        override fun update() {
            if(bigBombTimer == bigBombTimerMax) {
                bigBombList.forEach {
                    field.put(Pos(it.x, it.y), BlockBigBomb(BlockBigBomb.Position.BOTTOM_LEFT))
                    field.put(Pos(it.x + 1, it.y), BlockBigBomb(BlockBigBomb.Position.BOTTOM_RIGHT))
                    field.put(Pos(it.x, it.y + 1), BlockBigBomb(BlockBigBomb.Position.TOP_LEFT))
                    field.put(Pos(it.x + 1, it.y + 1), BlockBigBomb(BlockBigBomb.Position.TOP_RIGHT))
                    changeState(StateMoving())
                }
                bigBombList = ArrayList()
            } else bigBombTimer ++
        }
    }

    // BigBomb is size=-1
    class ExplosionData(val position: Pos, val size: Int) {}
    class CascadeData(val blocks: List<Pair<Pos, Block>>) {}
}