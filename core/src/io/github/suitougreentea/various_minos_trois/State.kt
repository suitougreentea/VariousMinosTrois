package io.github.suitougreentea.various_minos_trois

interface State {
  fun init() {}
  fun enter() {}
  fun update()
  fun leave() {}
  fun leaveOrSkip() {}
}

abstract class StateWithTimer: State {
  abstract val stateManager: StateManager
  abstract val frames: Int
  var timer = 0

  abstract fun nextState(): State

  override fun init() {
    if(frames == 0) stateManager.skipState(nextState())
  }

  override fun update() {
    timer ++
    if(timer == frames) stateManager.changeState(nextState())
  }
}

class StateManager() {
  var currentState: State = object: State {
    override fun update() {}
  }
  var nextState: State? = null

  fun update() {
    val nextState = nextState
    if(nextState != null) {
      currentState = nextState
      currentState.enter()
      this.nextState = null
    }

    currentState.update()
  }

  fun changeState(newState: State) {
    currentState.leave()
    currentState.leaveOrSkip()
    nextState = newState
    newState.init()
  }

  fun skipState(newState: State) {
    nextState?.leaveOrSkip()
    nextState = newState
    newState.init()
  }
}