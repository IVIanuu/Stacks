package com.ivianuu.stacks

import android.os.Parcelable
import com.ivianuu.stacks.internal.SavedState
import kotlin.reflect.KClass

interface Parceler {
    fun toParcelable(key: Any): Parcelable
    fun <T : Any> fromParcelable(type: KClass<T>, parcelable: Parcelable): T
}

internal fun <T : Any> Parceler.toSavedState(value: T): SavedState {
    val parceledValue = toParcelable(value)
    return SavedState(value.javaClass.name, parceledValue)
}

internal fun <T : Any> Parceler.fromSavedState(savedState: SavedState): T {
    val type = Class.forName(savedState.typeName).kotlin as KClass<T>
    return fromParcelable(type, savedState.value)
}