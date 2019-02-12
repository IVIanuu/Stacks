package com.ivianuu.stacks

import android.os.Bundle
import android.os.Parcelable
import com.ivianuu.stacks.internal.SavedState
import kotlinx.android.parcel.Parcelize

/**
 * @author Manuel Wrage (IVIanuu)
 */
class SavedStateRegistry(private val router: Router) {

    val entries: Map<Any, Any> get() = savedStates

    private val savedStates = mutableMapOf<Any, Any>()

    fun contains(key: Any): Boolean = savedStates.contains(key)

    operator fun set(key: Any, value: Any) {
        savedStates[key] = value
    }

    fun <T> get(key: Any): T? = savedStates[key] as? T

    fun remove(key: Any) {
        savedStates.remove(key)
    }

    internal fun saveInstanceState(): Bundle = Bundle().apply {
        val states = savedStates
            .map {
                router.parceler.toSavedState(it.key) to
                        router.parceler.toSavedState(it.value)
            }
            .map { StatePair(it.first, it.second) }

        putParcelableArrayList(KEY_SAVED_STATES, ArrayList(states))
    }

    internal fun restoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getParcelableArrayList<StatePair>(KEY_SAVED_STATES)!!
            .map {
                router.parceler.fromSavedState<Any>(it.savedKey) to
                        router.parceler.fromSavedState<Any>(it.savedValue)
            }
            .forEach { savedStates[it.first] = it.second }
    }

    @Parcelize
    private data class StatePair(
        val savedKey: SavedState,
        val savedValue: SavedState
    ) : Parcelable

    private companion object {
        private const val KEY_SAVED_STATES = "SavedStateRegistry.savedStates"
    }
}