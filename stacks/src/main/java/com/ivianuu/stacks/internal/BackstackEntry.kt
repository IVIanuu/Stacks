package com.ivianuu.stacks.internal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

internal data class BackstackEntry(
    val key: Any,
    var transactionIndex: Int = INVALID_INDEX
) {

    fun ensureValidIndex(indexer: TransactionIndexer) {
        if (transactionIndex == INVALID_INDEX) {
            transactionIndex = indexer.nextIndex()
        }
    }

    companion object {
        private const val INVALID_INDEX = -1
    }
}

@Parcelize
internal data class ParceledBackstackEntry(
    val parceledKey: Parcelable,
    val transactionIndex: Int
) : Parcelable