package ksqlite.kapi.impl.helpers

import ksqlite.capi.callbacks.Sqlite3DestroyCallback

/**
 * Destructor invoking [AutoCloseable.close] on the argument it receives.
 */
internal val AutoCloser = Sqlite3DestroyCallback{ value: AutoCloseable ->
    value.close()
}

/**
 * Returns [AutoCloser] if the [value] is an instance of [AutoCloseable].
 */
@PublishedApi
internal fun autoCloser(value: Any): Sqlite3DestroyCallback<Any>? {
    if (value is AutoCloseable) {
        @Suppress("UNCHECKED_CAST")
        return AutoCloser as Sqlite3DestroyCallback<Any>
    }

    return null
}