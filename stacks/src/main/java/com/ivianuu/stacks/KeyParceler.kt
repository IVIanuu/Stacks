package com.ivianuu.stacks

import android.os.Parcelable

/**
 * Converts keys to [Parcelable]'s and vice versa.
 */
interface KeyParceler {
    fun toParcelable(key: Any): Parcelable
    fun fromParcelable(parcelable: Parcelable): Any
}