package com.ivianuu.stacks.internal

import android.os.Parcelable
import com.ivianuu.stacks.Parceler
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal object DefaultParceler : Parceler {
    override fun toParcelable(key: Any): Parcelable = key as Parcelable
    override fun <T : Any> fromParcelable(type: KClass<T>, parcelable: Parcelable): T =
        parcelable as T
}