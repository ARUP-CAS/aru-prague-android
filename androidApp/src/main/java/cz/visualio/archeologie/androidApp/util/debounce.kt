package cz.visualio.archeologie.androidApp.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


fun <T> (suspend (T) -> Unit).debounce(time: Long, scope: CoroutineScope): suspend (T) -> Unit {
    val channel = Channel<T>(Channel.CONFLATED)
    val flow = channel.consumeAsFlow().debounce(time)

    scope.launch { flow.collect { this@debounce.invoke(it) } }

    return {
        channel.offer(it)
        Unit
    }
}