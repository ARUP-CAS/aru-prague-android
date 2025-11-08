package cz.visualio.archeologie.shared.util

import arrow.core.Either
import arrow.core.identity

sealed class NetworkStateMachine<out E, out T> {
    object Init : NetworkStateMachine<Nothing, Nothing>()
    object Loading : NetworkStateMachine<Nothing, Nothing>()
    data class Success<out T>(val value: T) : NetworkStateMachine<Nothing, T>()
    data class Failure<out E>(val error: E) : NetworkStateMachine<E, Nothing>()


    inline fun <R> fold(
        crossinline isInit: () -> R,
        crossinline isLoading: () -> R,
        crossinline isSuccess: (T) -> R,
        crossinline isFailure: (E) -> R,
    ): R = when (this) {
        Init -> isInit()
        Loading -> isLoading()
        is Success -> isSuccess(value)
        is Failure -> isFailure(error)
    }

    inline fun <E2, K> bimap(
        crossinline fe: (E) -> E2,
        crossinline fs: (T) -> K
    ): NetworkStateMachine<E2, K> = fold(
        isInit = { Init },
        isLoading = { Loading },
        isFailure = { Failure(fe(it)) },
        isSuccess = { Success(fs(it)) }
    )

    inline fun <K> mapLeft(crossinline f: (E) -> K): NetworkStateMachine<K, T> =
        bimap(f, ::identity)

    inline fun <K> mapRight(crossinline f: (T) -> K): NetworkStateMachine<E, K> =
        bimap(::identity, f)

    inline fun <K> map(crossinline f: (T) -> K): NetworkStateMachine<E, K> = mapRight(f)

    fun select(): Either<Unit, T> = fold(
        isInit = {Either.left(Unit)},
        isLoading = {Either.left(Unit)},
        isSuccess = {Either.right(it)},
        isFailure = {Either.left(Unit)}
    )


    val isSuccess by lazy {
        fold(
            isSuccess = {true},
            isFailure = {false},
            isLoading = {false},
            isInit = {false}
        )
    }
}


fun <A,B> Either<A, B>.toNetworkStateMachine(): NetworkStateMachine<A, B> = fold(
    ifLeft = {NetworkStateMachine.Failure(it)},
    ifRight = {NetworkStateMachine.Success(it)}
)

fun <T>NetworkStateMachine<*, T>.getOrElse(f: ()->T) = fold(
    isInit = f,
    isLoading = f,
    isSuccess = ::identity,
    isFailure = {f()}
)