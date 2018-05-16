package com.ivianuu.stacks

import android.os.Parcelable

/**
 * Default implementation of an [KeyParceler] this assumes that all your keys are [Parcelable]
 */
open class DefaultKeyParceler : KeyParceler {
    override fun toParcelable(key: Any): Parcelable = key as Parcelable
    override fun fromParcelable(parcelable: Parcelable): Any = parcelable
}