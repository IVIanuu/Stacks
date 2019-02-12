/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.stacks.sample.views

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.Parceler
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChanger

/**
 * @author Manuel Wrage (IVIanuu)
 */
class ViewStateChanger(
    private val container: ViewGroup,
    private val inflater: LayoutInflater,
    private val parceler: Parceler
) : StateChanger {

    private val savedStates = mutableMapOf<Any, Any>()

    override fun handleStateChange(stateChange: StateChange, listener: () -> Unit) {
        println("handle state change $stateChange")

        val topOldKey = stateChange.previousState.lastOrNull()
        if (topOldKey != null) {
            val oldView: View? = container.getChildAt(0)

            if (oldView != null) {
                if (stateChange.newState.contains(topOldKey)) {
                    val savedState = SparseArray<Parcelable>()
                    oldView.saveHierarchyState(savedState)

                    val bundle = Bundle()
                    bundle.putSparseParcelableArray("hierarchy", savedState)

                    savedStates[topOldKey] = bundle
                }

                container.removeView(oldView)
            }
        }

        val topNewKey = stateChange.newState.lastOrNull()
        if (topNewKey != null) {
            val view = inflater
                .inflate((topNewKey as ViewKey).layoutRes, container, false)

            if (view is BackstackView) {
                view.router = stateChange.router
                view.key = topNewKey
            }

            val bundle = savedStates[topNewKey] as? Bundle
            val savedState = bundle?.getSparseParcelableArray<Parcelable>("hierarchy")

            if (savedState != null) {
                view.restoreHierarchyState(savedState)
            }

            container.addView(view)
        }

        stateChange.previousState
            .filterNot { stateChange.newState.contains(it) }
            .forEach { savedStates.remove(it) }

        listener()
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedStates.clear()
        if (savedInstanceState == null) return

        val keys = savedInstanceState.getParcelableArrayList<Parcelable>(KEY_SAVED_STATE_KEYS)!!
            .map { parceler.fromParcelable(it) }
        val values = savedInstanceState.getParcelableArrayList<Parcelable>(KEY_SAVED_STATE_VALUES)!!

        keys.forEachIndexed { i, key -> savedStates[key] = values[i] }
    }

    fun saveInstanceState(): Bundle = Bundle().apply {
        putParcelableArrayList(KEY_SAVED_STATE_KEYS, ArrayList(
            savedStates.keys.map { parceler.toParcelable(it) }
        ))
        putParcelableArrayList(
            KEY_SAVED_STATE_VALUES, ArrayList(
                savedStates.keys.map { parceler.toParcelable(it) }
            ))
    }

    private companion object {
        private const val KEY_SAVED_STATE_KEYS = "ViewStateChanger.savedStateKeys"
        private const val KEY_SAVED_STATE_VALUES = "ViewStateChanger.savedStateValues"
    }
}