package io.github.suitougreentea.various_minos_trois

class MinoRandomizer {
    var minoSet: Set<Int> = setOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    /*fun init(minoSet: Set[Int]) {
        this.minoSet = minoSet
    }*/
    fun next() = minoSet.elementAt((Math.random() * minoSet.size).toInt())
    fun reset() = {}
}