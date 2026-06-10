package ksqlite.kapi.impl.functions

import ksqlite.capi.callbacks.Sqlite3DestroyCallback

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
internal fun <Data : Any> autoClosableDestructor(value: Data): Sqlite3DestroyCallback<Data>? {
    if (value is AutoCloseable) {
        @Suppress("UNCHECKED_CAST")
        return AutoClosableDestructor as Sqlite3DestroyCallback<Data>
    }

    return null
}