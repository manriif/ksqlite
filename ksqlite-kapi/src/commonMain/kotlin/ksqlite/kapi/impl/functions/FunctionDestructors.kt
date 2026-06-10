package ksqlite.kapi.impl.functions

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.kapi.functions.Function

/**
 * Destructor invoking [AutoCloseable.close] on the argument it receives.
 */
private val AutoClosableDestructor = Sqlite3DestroyCallback<Any> { value ->
    (value as AutoCloseable).close()
}

/**
 * Returns [AutoClosableDestructor] if the [value] is an instance of [AutoCloseable].
 */
@PublishedApi
internal fun autoClosableDestructor(value: Any): Sqlite3DestroyCallback<Any>? {
    if (value is AutoCloseable) {
        return AutoClosableDestructor
    }

    return null
}

/**
 * Destructor invoking [Function.destroy] on the argument it receives.
 */
internal val FunctionDestructor = Sqlite3DestroyCallback<Function> { function ->
    function.destroy()
}