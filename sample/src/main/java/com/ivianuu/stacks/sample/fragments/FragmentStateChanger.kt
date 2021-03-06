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

package com.ivianuu.stacks.sample.fragments

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChanger

class FragmentStateChanger(
    val fm: FragmentManager,
    val containerId: Int
) : StateChanger {

    override fun handleStateChange(
        stateChange: StateChange,
        listener: () -> Unit
    ) {
        val previousState = stateChange.previousState
        val newState = stateChange.newState

        val transaction = fm.beginTransaction()

        for (oldKey in previousState) {
            val fragment = fm.findFragmentByTag(getFragmentTag(oldKey))
            if (fragment != null) {
                if (!newState.contains(oldKey)) {
                    transaction.remove(fragment)
                } else if (!fragment.isDetached && newState.last() != oldKey) {
                    transaction.detach(fragment)
                }
            }
        }

        for (newKey in newState) {
            var fragment = fm.findFragmentByTag(getFragmentTag(newKey))
            if (newKey == stateChange.newState.last()) {
                if (fragment != null) {
                    if (fragment.isDetached) {
                        transaction.attach(fragment)
                    }
                } else {
                    fragment = createFragment(newKey)
                    transaction.add(containerId, fragment, getFragmentTag(newKey))
                }
            } else {
                if (fragment != null && !fragment.isDetached) {
                    transaction.detach(fragment)
                }
            }
        }

        transaction.commitNow()
        listener()
    }

    private fun createFragment(key: Any): Fragment =
        (key as? FragmentKey)?.createFragment() ?: error("key is not a fragment key")

    private fun getFragmentTag(key: Any) = key.toString()

}