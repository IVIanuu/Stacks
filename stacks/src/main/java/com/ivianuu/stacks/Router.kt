package com.ivianuu.stacks

import java.util.*

/**
 * Router
 */
class Router {

    var stateChanger: StateChanger? = null
        private set

    val backstack: List<Any> get() = _backstack
    private val _backstack = mutableListOf<Any>()

    private val queuedStateChanges = LinkedList<PendingStateChange>()

    var popsLastKey = false

    fun handleBack(): Boolean {
        if (queuedStateChanges.isNotEmpty()) {
            return true
        }

        if (_backstack.isEmpty()) return false

        if (!popsLastKey && _backstack.size < 2) return false

        popTop()

        return true
    }

    fun setBackstack(reducer: Reducer) {
        enqueueStateChange(reducer)
    }

    fun setStateChanger(stateChanger: StateChanger) {
        if (this.stateChanger != stateChanger) {
            this.stateChanger = stateChanger
            if (queuedStateChanges.isEmpty()) {
                enqueueStateChange { Result(it, Direction.REPLACE) }
            }
            if (queuedStateChanges.isEmpty()) {
                // todo rebind
            } else {
                beginStateChangeIfPossible()
            }
        }
    }

    fun removeStateChanger() {
        this.stateChanger = null
    }

    private fun enqueueStateChange(reducer: Reducer) {
        val pendingStateChange = PendingStateChange(reducer)
        queuedStateChanges.add(pendingStateChange)
        beginStateChangeIfPossible()
    }

    private fun beginStateChangeIfPossible(): Boolean {
        if (stateChanger != null && queuedStateChanges.isNotEmpty()) {
            val pendingStateChange = queuedStateChanges.first
            if (pendingStateChange.status == PendingStateChange.Status.ENQUEUED) {
                pendingStateChange.status = PendingStateChange.Status.IN_PROGRESS
                changeState(pendingStateChange)
                return true
            }
        }

        return false
    }

    private fun changeState(pendingStateChange: PendingStateChange) {
        val result = pendingStateChange.reducer(backstack.toMutableList())

        val stateChange = StateChange(backstack, result.newBackstack, result.direction, this)

        val completionListener = { completeStateChange(stateChange) }

        pendingStateChange.completionListener = completionListener

        val stateChanger = stateChanger ?: throw IllegalStateException("state changer is null")

        stateChanger.handleStateChange(stateChange, completionListener)
    }

    private fun completeStateChange(stateChange: StateChange) {
        _backstack.clear()
        _backstack.addAll(stateChange.newState)

        val pendingStateChange = queuedStateChanges.removeFirst()
        pendingStateChange.status = PendingStateChange.Status.COMPLETED

        beginStateChangeIfPossible()
    }


}

val Router.backstackSize get() = backstack.size

val Router.hasRoot get() = backstackSize > 0

fun Router.push(key: Any) {
    setBackstack { backstack ->
        backstack.add(key)
        Result(backstack, Direction.FORWARD)
    }
}

fun Router.replaceTop(key: Any) {
    setBackstack { backstack ->
        val newBackstack = backstack.toMutableList()
        if (newBackstack.isNotEmpty()) newBackstack.removeAt(newBackstack.lastIndex)
        newBackstack.add(key)
        Result(newBackstack, Direction.REPLACE)
    }
}

fun Router.pop(key: Any) {
    setBackstack { backstack ->
        backstack.removeAll { it == key }
        Result(backstack, Direction.BACKWARD)
    }
}

fun Router.pop(selector: (backstack: List<Any>) -> List<Any>) {
    setBackstack {
        val poppedKeys = selector(it)
        it.removeAll { poppedKeys.contains(it) }
        Result(it, Direction.BACKWARD)
    }
}

fun Router.popTop() {
    pop { it.lastOrNull()?.let { listOf(it) } ?: emptyList() }
}

fun Router.popTo(key: Any) {
    popTo { key }
}

fun Router.popTo(selector: (backstack: List<Any>) -> Any) {
    setBackstack {
        val key = selector(it)
        Result(backstack.dropLastWhile { it != key }, Direction.BACKWARD)
    }
}

fun Router.popToRoot() {
    setBackstack {
        val newBackstack = it.firstOrNull()?.let { listOf(it) } ?: emptyList()
        Result(newBackstack, Direction.BACKWARD)
    }
}

fun Router.setRoot(key: Any) {
    setBackstack { Result(listOf(key), Direction.FORWARD) }
}