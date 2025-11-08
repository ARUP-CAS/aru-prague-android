package cz.visualio.archeologie.shared.viewmodels

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrHandle
import arrow.fx.coroutines.ForkConnected
import arrow.fx.coroutines.evalOn
import kotlinx.coroutines.Dispatchers

typealias Reducer<ACTION, STATE> = (STATE, ACTION) -> STATE
typealias GetEffect<ACTION, STATE> = suspend (state: STATE, action: ACTION, dispatch: Dispatch<ACTION>) -> Either<Throwable, Unit>
typealias Dispatch<ACTION> = suspend (ACTION) -> Either<Throwable, Unit>


interface MVI<STATE, ACTION>: FlowStore<STATE> {
    val reducer: Reducer<ACTION, STATE>
    val getEffect: GetEffect<ACTION, STATE>
    val logError: suspend (Throwable) -> Either<Throwable, Unit>
    val logAction: suspend (ACTION) -> Either<Throwable, Unit>

    suspend fun dispatch(action: ACTION): Either<Throwable, Unit> = either {
        ForkConnected(Dispatchers.IO) {
            logAction(action).bind()
        }

        val newState = reducer(state, action)
        setState(newState)

        ForkConnected {
            getEffect(newState, action, ::dispatch).getOrHandle { evalOn(Dispatchers.IO) { logError(it) } }
        }
        Unit
    }

    companion object {
        operator fun <STATE, ACTION> invoke(
            reducer: Reducer<ACTION, STATE>,
            getEffect: GetEffect<ACTION, STATE>,
            logError: suspend (Throwable) -> Either<Throwable, Unit>,
            logAction: suspend (ACTION) -> Either<Throwable, Unit>,
            flowStore: FlowStore<STATE>
        ): MVI<STATE, ACTION> = object : MVI<STATE, ACTION>, FlowStore<STATE> by flowStore {
            override val reducer = reducer
            override val getEffect = getEffect
            override val logError = logError
            override val logAction = logAction
        }

        operator fun <STATE, ACTION> invoke(
            reducer: Reducer<ACTION, STATE>,
            getEffect: GetEffect<ACTION, STATE>,
            logError: suspend (Throwable) -> Either<Throwable, Unit>,
            logAction: suspend (ACTION) -> Either<Throwable, Unit>,
            initialState: STATE
        ) = invoke(
            reducer = reducer,
            getEffect = getEffect,
            logError = logError,
            logAction = logAction,
            flowStore = FlowStore(initialState)
        )
    }
}