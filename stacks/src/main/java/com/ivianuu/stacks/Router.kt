package com.ivianuu.stacks

import android.os.Bundle
import com.ivianuu.stacks.internal.BackstackEntry
import com.ivianuu.stacks.internal.DefaultParceler
import com.ivianuu.stacks.internal.DelegatingStateChanger
import com.ivianuu.stacks.internal.ParceledBackstackEntry
import com.ivianuu.stacks.internal.PendingStateChange
import com.ivianuu.stacks.internal.TransactionIndexer
import java.util.*
import kotlin.collections.ArrayList

/**
 * Router
 */
class Router(val parentRouter: Router? = null) {

    var parceler: Parceler = DefaultParceler

    var stateChanger: StateChanger? = null
        private set

    val backstack: List<Any> get() = _backstack.map { it.key }
    private val _backstack = mutableListOf<BackstackEntry>()

    private val queuedStateChanges = LinkedList<PendingStateChange>()

    private val transactionIndexer: TransactionIndexer by lazy {
        parentRouter?.transactionIndexer ?: TransactionIndexer()
    }

    private val isRootRouter get() = parentRouter != null

    var popsLastKey = false

    val savedStateRegistry = SavedStateRegistry(this)

    var paused = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    stateChanger?.onInactive(this)
                } else {
                    stateChanger?.onActive(this)
                }
                if (!value) {
                    beginStateChangeIfPossible()
                }
            }
        }

    fun handleBack(): Boolean {
        val canGoBack = (popsLastKey && backstack.isNotEmpty()
                || !popsLastKey && backstack.size > 1)
                || queuedStateChanges.isNotEmpty()
                && queuedStateChanges.last().status != PendingStateChange.Status.COMPLETED

        if (!canGoBack) return false

        setBackstack {
            if ((popsLastKey && it.isEmpty())
                || (!popsLastKey && it.size <= 1)
            ) {
                // noop
                Result.NOOP
            } else {
                it.removeAt(it.lastIndex)
                Result(it, Direction.BACKWARD)
            }
        }

        return true
    }

    fun setBackstack(reducer: Reducer) {
        enqueueStateChange(reducer)
    }

    fun setStateChanger(stateChanger: StateChanger) {
        if (this.stateChanger != stateChanger) {
            removeStateChanger()
            this.stateChanger = DelegatingStateChanger(stateChanger).also {
                it.onAttach(this)
                if (!paused) it.onActive(this)
            }

            if (queuedStateChanges.isEmpty()) {
                // todo rebind
                enqueueStateChange { Result(it, Direction.REPLACE) }
            } else {
                beginStateChangeIfPossible()
            }
        }
    }

    fun removeStateChanger() {
        stateChanger?.let {
            it.onInactive(this)
            it.onDetach(this)
        }
        stateChanger = null
    }

    fun saveInstanceState(): Bundle = Bundle().apply {
        putParcelableArrayList(
            KEY_BACKSTACK,
            ArrayList(
                _backstack
                    .map {
                        ParceledBackstackEntry(
                            parceler.toSavedState(it.key),
                            it.transactionIndex
                        )
                    }
            )
        )
        putBoolean(KEY_POPS_LAST_KEY, popsLastKey)

        stateChanger?.onSaveInstanceState(this@Router, _backstack, savedStateRegistry)
        putBundle(KEY_SAVED_STATE_MANAGER, savedStateRegistry.saveInstanceState())

        if (isRootRouter) {
            putBundle(KEY_TRANSACTION_INDEXER, transactionIndexer.saveInstanceState())
        }
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        _backstack.clear()
        _backstack.addAll(
            savedInstanceState.getParcelableArrayList<ParceledBackstackEntry>(KEY_BACKSTACK)!!
                .map {
                    BackstackEntry(
                        parceler.fromSavedState(it.savedKey),
                        it.transactionIndex
                    )
                }
        )

        popsLastKey = savedInstanceState.getBoolean(KEY_POPS_LAST_KEY)

        savedStateRegistry.restoreInstanceState(
            savedInstanceState.getBundle(KEY_SAVED_STATE_MANAGER)!!
        )

        if (isRootRouter) {
            transactionIndexer.restoreInstanceState(
                savedInstanceState.getBundle(KEY_TRANSACTION_INDEXER)!!
            )
        }
    }

    private fun enqueueStateChange(reducer: Reducer) {
        val pendingStateChange = PendingStateChange(reducer)
        queuedStateChanges.add(pendingStateChange)
        beginStateChangeIfPossible()
    }

    private fun beginStateChangeIfPossible(): Boolean {
        if (stateChanger != null && !paused && queuedStateChanges.isNotEmpty()) {
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
        val stateChanger = stateChanger ?: error("state changer is null")

        val result = pendingStateChange.reducer(backstack.toMutableList())

        // noop
        if (result == Result.NOOP) {
            queuedStateChanges.removeFirst()
            pendingStateChange.status = PendingStateChange.Status.COMPLETED
            return
        }

        val stateChange = StateChange(backstack, result.newBackstack, result.direction, this)

        val completionListener = { completeStateChange(stateChange) }

        pendingStateChange.completionListener = completionListener

        stateChanger.handleStateChange(stateChange, completionListener)
    }

    private fun completeStateChange(stateChange: StateChange) {
        val newBackstack = stateChange.newState
            .map { key ->
                _backstack.firstOrNull { it.key === key }
                    ?: BackstackEntry(key)
            }

        // Swap around transaction indices to ensure they don't get thrown out of order by the
        // developer rearranging the backstack at runtime.
        val indices = newBackstack
            .onEach { it.ensureValidIndex(transactionIndexer) }
            .map { it.transactionIndex }
            .sorted()

        newBackstack.forEachIndexed { i, transaction ->
            transaction.transactionIndex = indices[i]
        }

        _backstack.clear()
        _backstack.addAll(newBackstack)

        val pendingStateChange = queuedStateChanges.removeFirst()
        pendingStateChange.status = PendingStateChange.Status.COMPLETED

        beginStateChangeIfPossible()
    }

    private companion object {
        private const val KEY_BACKSTACK = "Router.backstack"
        private const val KEY_POPS_LAST_KEY = "Router.popsLastKey"
        private const val KEY_SAVED_STATE_MANAGER = "Router.savedStateRegistry"
        private const val KEY_TRANSACTION_INDEXER = "Router.transactionIndexer"
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