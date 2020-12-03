package cz.visualio.archeologie.shared.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface FlowStore<STATE> {
    fun setState(newState: STATE)
    val state: STATE
    val flow: StateFlow<STATE>

    companion object {
        operator fun <STATE> invoke(initialState: STATE): FlowStore<STATE> {
            val mutableFlow = MutableStateFlow(initialState)
            return object : FlowStore<STATE> {
                override fun setState(newState: STATE) {
                    mutableFlow.value = newState
                }

                override val state: STATE
                    get() = mutableFlow.value
                override val flow: StateFlow<STATE>
                    get() = mutableFlow
            }
        }
    }
}