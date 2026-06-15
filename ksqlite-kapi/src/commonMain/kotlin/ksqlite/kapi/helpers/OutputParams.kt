package ksqlite.kapi.helpers

import ksqlite.capi.types.OutputParam
import kotlin.jvm.JvmName

/**
 * Invokes [block] and returns [param]'s value if [block] returns without throwing.
 */
internal inline fun <V: Any, P : OutputParam<V>> usingParam(
    param: P,
    block: (P) -> Unit
): V {
    block(param)
    return param.value
}

/**
 * Invokes [block] and returns [param]'s value if [block] returns without throwing.
 */
@JvmName("usingParam2")
internal inline fun <V: Any, P : OutputParam<V?>> usingParam(
    param: P,
    block: (P) -> Unit
): V {
    block(param)
    return checkNotNull(param.value) { "Parameter's value is null" }
}

/**
 * Invokes [block] and returns [param1]'s [V1] paired with [param2]'s [V2] if [block] returns
 * without throwing.
 */
internal inline fun <V1: Any, P1 : OutputParam<V1>, V2: Any, P2 : OutputParam<V2>> usingParams(
    param1: P1,
    param2: P2,
    block: (P1, P2) -> Unit
): Pair<V1, V2> {
    block(param1, param2)
    return param1.value to param2.value
}

/**
 * Invokes [block] and returns [param1]'s [V1] paired with [param2]'s [V2] if [block] returns
 * without throwing.
 */
@JvmName("usingParams2")
internal inline fun <V1: Any, P1 : OutputParam<V1?>, V2: Any, P2 : OutputParam<V2>> usingParams(
    param1: P1,
    param2: P2,
    block: (P1, P2) -> Unit
): Pair<V1, V2> {
    block(param1, param2)
    return checkNotNull(param1.value) { "First parameter's value is null" } to param2.value
}

/**
 * Invokes [block] and returns [param1]'s [V1] paired with [param2]'s [V2] if [block] returns
 * without throwing.
 */
@JvmName("usingParams3")
internal inline fun <V1: Any, P1 : OutputParam<V1?>, V2: Any, P2 : OutputParam<V2?>> usingParams(
    param1: P1,
    param2: P2,
    block: (P1, P2) -> Unit
): Pair<V1, V2> {
    block(param1, param2)

    return checkNotNull(param1.value) { "First parameter's value is null" } to
            checkNotNull(param2.value) { "Second parameter's value is null" }
}