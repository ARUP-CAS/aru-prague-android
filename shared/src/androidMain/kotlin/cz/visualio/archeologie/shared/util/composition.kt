package cz.visualio.archeologie.shared.util

fun <A,B,C,D,R> (suspend (A,B,C,D) -> R).partially4(d: D): suspend (A,B,C) -> R = { a,b,c -> this.invoke(a,b,c,d) }