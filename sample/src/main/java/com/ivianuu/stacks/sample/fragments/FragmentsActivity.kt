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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ivianuu.stacks.Backstack

class FragmentsActivity : AppCompatActivity() {

    lateinit var backstack: Backstack

    private val stateChanger by lazy(LazyThreadSafetyMode.NONE) {
        FragmentStateChanger(
            supportFragmentManager,
            android.R.id.content
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backstack = Backstack.newBuilder()
            .initialKeys(DummyFragmentKey(1))
            .savedInstanceState(savedInstanceState)
            .stateChanger(stateChanger)
            .build()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        backstack.setStateChanger(stateChanger)
    }

    override fun onPause() {
        backstack.removeStateChanger()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        backstack.saveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (!backstack.goBack()) {
            super.onBackPressed()
        }
    }
}
