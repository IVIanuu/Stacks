package com.ivianuu.stacks

import android.os.Bundle
import android.os.Parcelable
import java.util.*

/**
 * Backstack
 */
class Backstack internal constructor(
    var keyFilter: KeyFilter,
    val initialKeys: List<Any>,
    var keyParceler: KeyParceler,
    val tag: String,
    stateChangeListeners: Collection<StateChangeListener>
) {

    private var stateChanger: StateChanger? = null

    private val backstack = mutableListOf<Any>()

    private val queuedStateChanges = LinkedList<PendingStateChange>()

    private val stateChangeListeners = mutableListOf<StateChangeListener>()

    init {
        this.stateChangeListeners.addAll(stateChangeListeners)
    }

    fun goTo(newKey: Any) {
        val newBackstack = mutableListOf<Any>()
        val activeBackstack = selectActiveBackstack()

        if (activeBackstack.isNotEmpty()
            && activeBackstack.last() == newKey) {
            newBackstack.addAll(activeBackstack)
            enqueueStateChange(newBackstack, Direction.REPLACE)
            return
        }

        var isNewKey = true
        for (key in activeBackstack) {
            newBackstack.add(key)
            if (key == newKey) {
                isNewKey = false
                break
            }
        }
        val direction = if (isNewKey) {
            newBackstack.add(newKey)
            Direction.FORWARD
        } else {
            Direction.BACKWARD
        }

        enqueueStateChange(newBackstack, direction)
    }

    fun replaceTop(newTop: Any, direction: Direction = Direction.REPLACE) {
        val newBackstack = selectActiveBackstack().toMutableList()
        if (newBackstack.isNotEmpty()) {
            newBackstack.removeAt(newBackstack.lastIndex)
        }
        newBackstack.add(newTop)
        setBackstack(newBackstack, direction)
    }

    fun goUp(newKey: Any) {
        val activeBackstack = selectActiveBackstack()

        if (activeBackstack.size <= 1) {
            replaceTop(newKey, Direction.BACKWARD)
            return
        }
        if (activeBackstack.contains(newKey)) {
            goTo(newKey)
        } else {
            replaceTop(newKey, Direction.FORWARD)
        }
    }

    fun goBack(): Boolean {
        if (hasPendingStateChange()) {
            return true
        }

        if (backstack.size <= 1) {
            return false
        }

        val newBackstack = mutableListOf<Any>()
        val activeBackstack = selectActiveBackstack()
        (0 until activeBackstack.size - 1).mapTo(newBackstack) { activeBackstack[it] }
        enqueueStateChange(newBackstack, Direction.BACKWARD)

        return true
    }

    fun jumpToRoot(): Boolean {
        val activeBackstack = selectActiveBackstack()

        if (activeBackstack.size <= 1) {
            return false
        }

        setBackstack(listOf(activeBackstack.first()), Direction.REPLACE)
        return true
    }

    fun setBackstack(newBackstack: List<Any>, direction: Direction = Direction.REPLACE) {
        enqueueStateChange(newBackstack, direction)
    }

    fun getBackstack() =
        backstack.toList()

    fun setStateChanger(stateChanger: StateChanger) {
        if (this.stateChanger != stateChanger) {
            this.stateChanger = stateChanger
            if (!hasPendingStateChange()) {
                if (backstack.isEmpty()) {
                    val newHistory = selectActiveBackstack()
                    backstack.addAll(initialKeys)
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
                .map { keyParceler.fromParcelable(it) }
        setBackstack(keys)
    }

    fun saveInstanceState(outState: Bundle) {
        val filteredBackstack = keyFilter.filter(backstack)
        val parcelledKeys = filteredBackstack.map { keyParceler.toParcelable(it) }
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
        return if(backstack.isEmpty() && queuedStateChanges.size <= 0) {
            initialKeys.toList()
        } else if(queuedStateChanges.isEmpty()) {
            backstack.toList()
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
            backstack.toList()
        }

        val stateChange = StateChange(
            previousState,
            newBackstack,
            direction,
            this
        )

        val completionCallback = object : StateChanger.Callback {
            override fun onCompleted() {
                completeStateChange(stateChange)
            }
        }

        pendingStateChange.completionCallback = completionCallback

        stateChangeListeners.forEach { it.preStateChange(stateChange) }

        stateChanger!!.handleStateChange(stateChange, completionCallback)
    }

    private fun completeStateChange(stateChange: StateChange) {
        backstack.clear()
        backstack.addAll(stateChange.newState)

        val pendingStateChange = queuedStateChanges.removeFirst()
        pendingStateChange.status = PendingStateChange.Status.COMPLETED

        stateChangeListeners.forEach { it.postStateChange(stateChange) }

        beginStateChangeIfPossible()
    }

    private fun hasPendingStateChange() = queuedStateChanges.isNotEmpty()

    class Builder internal constructor() {
        private val initialKeys = mutableListOf<Any>()
        private var savedInstanceState: Bundle? = null
        private var keyParceler: KeyParceler? = null
        private var keyFilter: KeyFilter? = null
        private var tag: String? = null
        private var stateChanger: StateChanger? = null
        private val stateChangeListeners = mutableListOf<StateChangeListener>()

        fun initialKeys(vararg initialKeys: Any): Builder {
            this.initialKeys.addAll(initialKeys)
            return this
        }

        fun initialKeys(initialKeys: Collection<Any>): Builder {
            this.initialKeys.addAll(initialKeys)
            return this
        }

        fun savedInstanceState(savedInstanceState: Bundle?): Builder {
            this.savedInstanceState = savedInstanceState
            return this
        }

        fun keyParceler(keyParceler: KeyParceler): Builder {
            this.keyParceler = keyParceler
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

        fun build(): Backstack {
            if (initialKeys.isEmpty()) {
                throw IllegalStateException("at least one initial key must be set")
            }

            val keyParceler = keyParceler ?: DefaultKeyParceler()
            val backstackFilter = keyFilter ?: DefaultKeyFilter()
            val tag = tag ?: ""

            return Backstack(backstackFilter, initialKeys,
                keyParceler, tag, stateChangeListeners).apply {
                val savedInstanceState = savedInstanceState
                if (savedInstanceState != null) {
                    restoreInstanceState(savedInstanceState)
                }

                val stateChanger = stateChanger
                if (stateChanger != null) {
                    setStateChanger(stateChanger)
                }
            }
        }

    }

    companion object {
        private const val KEY_BACKSTACK = "Backstack.backstack"

        fun newBuilder() = Builder()
    }
}