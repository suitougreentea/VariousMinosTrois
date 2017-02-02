package io.github.suitougreentea.various_minos_trois.game

interface State {
  fun enter() {}
  fun update()
  fun leave() {}
}

abstract class StateWithTimer: State {
  abstract val stateManager: StateManager
  abstract val frames: Int
  var timer = 0

  abstract fun nextState(): State

  override fun update() {
    if(timer == frames) stateManager.changeState(nextState())
    else timer ++
  }
}

class StateManager() {
  var currentState: State = object: State {
    override fun update() {}
  }

  fun update() {
    val currentState = currentState
    currentState.update()
  }

  fun changeState(newState: State) {
    currentState.leave()
    currentState = newState
    currentState.enter()
    currentState.update()
  }
}