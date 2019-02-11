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
import com.ivianuu.stacks.Router
import com.ivianuu.stacks.hasRoot
import com.ivianuu.stacks.setRoot

class FragmentsActivity : AppCompatActivity() {

    lateinit var router: Router

    private val stateChanger by lazy(LazyThreadSafetyMode.NONE) {
        FragmentStateChanger(
            supportFragmentManager,
            android.R.id.content
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        router = Router()
        router.setStateChanger(stateChanger)

        if (!router.hasRoot) {
            router.setRoot(DummyFragmentKey(1))
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        router.setStateChanger(stateChanger)
    }

    override fun onPause() {
        router.removeStateChanger()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // router.saveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }
}
