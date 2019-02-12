package com.ivianuu.stacks.internal

import android.os.Parcelable
import com.ivianuu.stacks.Parceler

@Suppress("UNCHECKED_CAST")
internal object DefaultParceler : Parceler {
    override fun toParcelable(key: Any): Parcelable = key as Parcelable
    override fun fromParcelable(parceledKey: Parcelable): Any = parceledKey
}