package com.ivianuu.stacks

/**
 * Reflects the state of the router
 */
interface StateChanger {

    fun handleStateChange(stateChange: StateChange, listener: () -> Unit)

    fun onAttach(router: Router) {
    }

    fun onActive(router: Router) {
    }

    fun onInactive(router: Router) {
    }

    fun onDetach(router: Router) {
    }

    fun onSaveInstanceState(
        router: Router,
        backstack: List<Any>,
        registry: SavedStateRegistry
    ) {
    }

}