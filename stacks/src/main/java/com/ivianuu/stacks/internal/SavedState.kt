package com.ivianuu.stacks.internal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SavedState(
    val typeName: String,
    val value: Parcelable
) : Parcelable