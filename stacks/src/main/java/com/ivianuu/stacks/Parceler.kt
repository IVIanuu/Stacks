package com.ivianuu.stacks

import android.os.Parcelable

interface Parceler {
    fun toParcelable(key: Any): Parcelable
    fun fromParcelable(parceledKey: Parcelable): Any
}