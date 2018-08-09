package com.ivianuu.stacks

import android.os.Parcelable

/**
 * Converts keys to [Parcelable]'s and vice versa.
 */
interface KeyParceler {
    fun toParcelable(key: Any): Parcelable
    fun fromParcelable(parcelable: Parcelable): Any
}

/**
 * Default implementation of an [KeyParceler] this assumes that all your keys are [Parcelable]
 */
class DefaultKeyParceler : KeyParceler {
    override fun toParcelable(key: Any): Parcelable = key as Parcelable
    override fun fromParcelable(parcelable: Parcelable): Any = parcelable
}