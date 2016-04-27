package io.github.suitougreentea.various_minos_trois

interface State {
    fun enter() {}
    fun update()
    fun leave() {}
}

class StateManager(firstState: State) {
    var currentState = firstState
    fun update() {
        currentState.update()
    }

    fun changeState(newState: State) {
        currentState.leave()
        currentState = newState
        currentState.enter()
        currentState.update()
    }
}