package com.ivianuu.stacks.internal

import com.ivianuu.stacks.Router
import com.ivianuu.stacks.StateChanger

internal class DelegatingStateChanger(
    private val stateChanger: StateChanger
) : StateChanger by stateChanger {

    private var attached = false
    private var active = false

    override fun onAttach(router: Router) {
        if (!attached) {
            attached = true
            stateChanger.onAttach(router)
        }
    }

    override fun onActive(router: Router) {
        if (!active) {
            active = true
            stateChanger.onActive(router)
        }
    }

    override fun onInactive(router: Router) {
        if (active) {
            active = false
            stateChanger.onInactive(router)
        }
    }

    override fun onDetach(router: Router) {
        if (attached) {
            attached = false
            stateChanger.onDetach(router)
        }
    }
}