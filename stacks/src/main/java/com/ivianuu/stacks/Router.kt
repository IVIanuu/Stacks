package com.ivianuu.stacks

import android.os.Bundle
import android.os.Parcelable
import java.util.*

/**
 * Router
 */
class Router internal constructor(
    var keyFilter: KeyFilter,
    var parceler: Parceler,
    val tag: String
) {

    var stateChanger: StateChanger? = null
        private set

    val backstack: List<Any> get() = _backstack
    private val _backstack = mutableListOf<Any>()

    private val queuedStateChanges = LinkedList<PendingStateChange>()

    private val stateChangeListeners = mutableListOf<StateChangeListener>()

    fun goBack(): Boolean {
        if (hasPendingStateChange()) {
            return true
        }

        if (_backstack.size <= 1) {
            return false
        }

        val newBackstack = mutableListOf<Any>()
        val activeBackstack = selectActiveBackstack()
        (0 until activeBackstack.size - 1).mapTo(newBackstack) { activeBackstack[it] }
        enqueueStateChange(newBackstack, Direction.BACKWARD)

        return true
    }

    fun setBackstack(newBackstack: List<Any>, direction: Direction = Direction.REPLACE) {
        enqueueStateChange(newBackstack, direction)
    }

    fun setStateChanger(stateChanger: StateChanger) {
        if (this.stateChanger != stateChanger) {
            this.stateChanger = stateChanger
            if (!hasPendingStateChange()) {
                if (_backstack.isEmpty()) {
                    val newHistory = selectActiveBackstack()
                    // todo _backstack.addAll(initialKeys)
                    enqueueStateChange(newHistory, Direction.REPLACE, true)
                }
            } else {
                beginStateChangeIfPossible()
            }
        }
    }

    fun removeStateChanger() {
        this.stateChanger = null
    }

    fun addStateChangeListener(listener: StateChangeListener) {
        if (!stateChangeListeners.contains(listener)) {
            stateChangeListeners.add(listener)
        }
    }

    fun removeStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.remove(listener)
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        val keys =
            savedInstanceState.getParcelableArrayList<Parcelable>(KEY_BACKSTACK + tag)
                .map { parceler.fromParcelable(it) }
        setBackstack(keys)
    }

    fun saveInstanceState(outState: Bundle) {
        val filteredBackstack = keyFilter.filter(_backstack)
        val parcelledKeys = filteredBackstack.map { parceler.toParcelable(it) }
        outState.putParcelableArrayList(KEY_BACKSTACK + tag, ArrayList(parcelledKeys))
    }

    private fun enqueueStateChange(
        newBackstack: List<Any>,
        direction: Direction,
        init: Boolean = false
    ) {
        val pendingStateChange = PendingStateChange(newBackstack.toList(), direction, init)
        queuedStateChanges.add(pendingStateChange)

        beginStateChangeIfPossible()
    }

    private fun selectActiveBackstack(): List<Any> {
        return if (queuedStateChanges.isEmpty()) {
            _backstack.toList()
        } else {
            queuedStateChanges.last.newBackstack.toList()
        }
    }

    private fun beginStateChangeIfPossible(): Boolean {
        if (stateChanger != null && hasPendingStateChange()) {
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
        val newBackstack = pendingStateChange.newBackstack
        val direction = pendingStateChange.direction
        val init = pendingStateChange.init

        val previousState = if (init) {
            emptyList()
        } else {
            _backstack.toList()
        }

        val stateChange = StateChange(
            previousState,
            newBackstack,
            direction,
            this
        )

        val completionListener = { completeStateChange(stateChange) }

        pendingStateChange.completionListener = completionListener

        stateChangeListeners.forEach { it.preStateChange(stateChange) }

        val stateChanger = stateChanger ?: throw IllegalStateException("state changer is null")

        stateChanger.handleStateChange(stateChange, completionListener)
    }

    private fun completeStateChange(stateChange: StateChange) {
        _backstack.clear()
        _backstack.addAll(stateChange.newState)

        val pendingStateChange = queuedStateChanges.removeFirst()
        pendingStateChange.status = PendingStateChange.Status.COMPLETED

        stateChangeListeners.forEach { it.postStateChange(stateChange) }

        beginStateChangeIfPossible()
    }

    private fun hasPendingStateChange() = queuedStateChanges.isNotEmpty()

    class Builder internal constructor() {
        private var savedInstanceState: Bundle? = null
        private var parceler: Parceler? = null
        private var keyFilter: KeyFilter? = null
        private var tag: String? = null
        private var stateChanger: StateChanger? = null
        private val stateChangeListeners = mutableListOf<StateChangeListener>()

        fun savedInstanceState(savedInstanceState: Bundle?): Builder {
            this.savedInstanceState = savedInstanceState
            return this
        }

        fun keyParceler(parceler: Parceler): Builder {
            this.parceler = parceler
            return this
        }

        fun keyFilter(keyFilter: KeyFilter): Builder {
            this.keyFilter = keyFilter
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun stateChanger(stateChanger: StateChanger): Builder {
            this.stateChanger = stateChanger
            return this
        }

        fun addStateChangeListeners(vararg stateChangeListeners: StateChangeListener): Builder {
            this.stateChangeListeners.addAll(stateChangeListeners)
            return this
        }

        fun addStateChangeListeners(stateChangeListeners: Collection<StateChangeListener>): Builder {
            this.stateChangeListeners.addAll(stateChangeListeners)
            return this
        }

        fun build(): Router {
            val keyParceler = parceler ?: DefaultParceler()
            val backstackFilter = keyFilter ?: DefaultKeyFilter()
            val tag = tag ?: ""

            val backstack = Router(
                backstackFilter,
                keyParceler, tag
            )

            val savedInstanceState = savedInstanceState
            if (savedInstanceState != null) {
                backstack.restoreInstanceState(savedInstanceState)
            }

            val stateChanger = stateChanger
            if (stateChanger != null) {
                backstack.setStateChanger(stateChanger)
            }

            return backstack
        }

    }

    companion object {
        private const val KEY_BACKSTACK = "Router.router"

        fun newBuilder() = Builder()
    }
}

val Router.backstackSize get() = backstack.size

val Router.hasRoot get() = backstackSize > 0

fun Router.push(key: Any) {
    val newBackstack = backstack.toMutableList()
    newBackstack.add(key)
    setBackstack(newBackstack, Direction.FORWARD)
}

fun Router.replaceTop(key: Any) {
    val newBackstack = backstack.toMutableList()
    if (newBackstack.isNotEmpty()) newBackstack.removeAt(newBackstack.lastIndex)
    newBackstack.add(key)
    setBackstack(newBackstack, Direction.REPLACE)
}

fun Router.pop(key: Any) {
    val newBackstack = backstack.toMutableList()
    newBackstack.removeAll { it == key }
    setBackstack(newBackstack, Direction.BACKWARD)
}

fun Router.popTop() {
    backstack.lastOrNull()?.let { pop(it) }
}

fun Router.popTo(key: Any) {
    val newBackstack = backstack.dropLastWhile { it != key }
    setBackstack(newBackstack, Direction.BACKWARD)
}

fun Router.popToRoot() {
    backstack.firstOrNull()?.let { popTo(it) }
}

fun Router.setRoot(key: Any) {
    setBackstack(listOf(key), Direction.FORWARD)
}